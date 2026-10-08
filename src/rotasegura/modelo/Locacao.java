package rotasegura.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import rotasegura.excecoes.DadosInvalidosException;
import rotasegura.excecoes.PeriodoInvalidoException;
import rotasegura.excecoes.RotaSeguraException;
import rotasegura.relatorio.Imprimivel;

/** Contrato de locação. Também é um documento imprimível (comprovante). */
public class Locacao implements Imprimivel {
    public enum Status { ABERTA, FINALIZADA }

    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final int id;
    private final Cliente cliente;
    private final Veiculo veiculo;
    private final LocalDate inicio;
    private final LocalDate fimPrevisto;
    private final double kmSaida;
    private LocalDate devolucao;
    private double kmChegada;
    private double valorDiarias;
    private double valorSeguro;
    private double valorMulta;
    private Status status = Status.ABERTA;

    private Locacao(int id, Cliente cliente, Veiculo veiculo, LocalDate inicio, LocalDate fimPrevisto, double kmSaida) {
        this.id = id;
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.inicio = inicio;
        this.fimPrevisto = fimPrevisto;
        this.kmSaida = kmSaida;
    }

    /** Abre um contrato: valida datas, reserva o veículo e calcula diárias e seguro (polimorfismo). */
    public static Locacao abrir(int id, Cliente cliente, Veiculo veiculo, LocalDate inicio, LocalDate fimPrevisto)
            throws RotaSeguraException {
        if (cliente == null || veiculo == null) throw new DadosInvalidosException("Cliente e veículo são obrigatórios.");
        if (inicio == null || fimPrevisto == null) throw new PeriodoInvalidoException("Datas de início e fim são obrigatórias.");
        if (inicio.isBefore(LocalDate.now())) throw new PeriodoInvalidoException("A data de início não pode estar no passado.");
        if (!fimPrevisto.isAfter(inicio))
            throw new PeriodoInvalidoException("A devolução prevista deve ser posterior à data de início.");
        veiculo.alugar(); // lança VeiculoIndisponivelException se ocupado/em manutenção
        Locacao l = new Locacao(id, cliente, veiculo, inicio, fimPrevisto, veiculo.getQuilometragem());
        int dias = (int) ChronoUnit.DAYS.between(inicio, fimPrevisto);
        l.valorDiarias = veiculo.calcularDiarias(dias);
        l.valorSeguro = veiculo.calcularSeguro(dias);
        return l;
    }

    /** Fecha o contrato: cobra multa se houve atraso e libera o veículo. */
    public void encerrar(LocalDate dataDevolucao, double kmChegada) throws RotaSeguraException {
        if (status != Status.ABERTA) throw new DadosInvalidosException("A locação #" + id + " já foi encerrada.");
        if (dataDevolucao == null || dataDevolucao.isBefore(inicio))
            throw new PeriodoInvalidoException("A data de devolução não pode ser anterior ao início da locação.");
        if (kmChegada < kmSaida)
            throw new DadosInvalidosException("Km de chegada não pode ser menor que o de saída (" + kmSaida + ").");
        veiculo.devolver(kmChegada);
        int atraso = (int) Math.max(0, ChronoUnit.DAYS.between(fimPrevisto, dataDevolucao));
        this.valorMulta = veiculo.calcularMultaAtraso(atraso);
        this.devolucao = dataDevolucao;
        this.kmChegada = kmChegada;
        this.status = Status.FINALIZADA;
    }

    public int getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Veiculo getVeiculo() { return veiculo; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFimPrevisto() { return fimPrevisto; }
    public Status getStatus() { return status; }
    public boolean isFinalizada() { return status == Status.FINALIZADA; }
    public double getValorTotal() { return valorDiarias + valorSeguro + valorMulta; }

    @Override
    public String gerarDocumento() {
        StringBuilder sb = new StringBuilder();
        sb.append("=============== ROTA SEGURA - ").append(isFinalizada() ? "COMPROVANTE DE LOCAÇÃO" : "CONTRATO DE LOCAÇÃO")
                .append(" ===============\n");
        sb.append(String.format("Contrato nº %d  |  Situação: %s%n", id, status));
        sb.append(String.format("Cliente : %s (CPF %s)%n", cliente.getNome(), cliente.getCpfFormatado()));
        sb.append(String.format("Veículo : %s %s - placa %s (%s)%n", veiculo.getCategoria(), veiculo.getModelo(),
                veiculo.getPlaca(), veiculo.getAno()));
        sb.append(String.format("Período : %s a %s (previsto)%n", inicio.format(FMT), fimPrevisto.format(FMT)));
        if (isFinalizada()) {
            sb.append(String.format(BR, "Devolução: %s | Km: %,.0f -> %,.0f%n", devolucao.format(FMT), kmSaida, kmChegada));
        }
        sb.append(String.format(BR, "  Diárias ........ R$ %,10.2f%n", valorDiarias));
        sb.append(String.format(BR, "  Seguro ......... R$ %,10.2f%n", valorSeguro));
        if (isFinalizada()) sb.append(String.format(BR, "  Multa atraso ... R$ %,10.2f%n", valorMulta));
        sb.append(String.format(BR, "  TOTAL%s R$ %,10.2f%n", isFinalizada() ? " .......... " : " (previsto) ", getValorTotal()));
        sb.append("==========================================================");
        return sb.toString();
    }

    // ---- persistência ----
    public String toLinha() {
        return String.join("|", String.valueOf(id), cliente.getCpf(), veiculo.getPlaca(), inicio.toString(),
                fimPrevisto.toString(), devolucao == null ? "" : devolucao.toString(), String.valueOf(kmSaida),
                String.valueOf(kmChegada), String.valueOf(valorDiarias), String.valueOf(valorSeguro),
                String.valueOf(valorMulta), status.name());
    }

    /** Reconstrói um contrato salvo (campos já separados por '|', 12 posições). */
    public static Locacao restaurar(String[] p, Cliente c, Veiculo v) throws DadosInvalidosException {
        try {
            Locacao l = new Locacao(Integer.parseInt(p[0]), c, v, LocalDate.parse(p[3]), LocalDate.parse(p[4]),
                    Double.parseDouble(p[6]));
            l.devolucao = p[5].isEmpty() ? null : LocalDate.parse(p[5]);
            l.kmChegada = Double.parseDouble(p[7]);
            l.valorDiarias = Double.parseDouble(p[8]);
            l.valorSeguro = Double.parseDouble(p[9]);
            l.valorMulta = Double.parseDouble(p[10]);
            l.status = Status.valueOf(p[11]);
            return l;
        } catch (RuntimeException e) {
            throw new DadosInvalidosException("Linha de locação corrompida (contrato " + p[0] + "): " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return String.format(BR, "#%d | %-25s | %-8s %-8s | %s -> %s | R$ %,.2f | %s", id, cliente.getNome(),
                veiculo.getCategoria(), veiculo.getPlaca(), inicio.format(FMT), fimPrevisto.format(FMT),
                getValorTotal(), status);
    }
}
