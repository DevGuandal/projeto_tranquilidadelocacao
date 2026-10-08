package rotasegura.modelo;

import java.time.Year;
import java.util.Locale;

import rotasegura.excecoes.DadosInvalidosException;
import rotasegura.excecoes.VeiculoIndisponivelException;
import rotasegura.util.Validacao;

/**
 * Classe-base abstrata dos veículos.
 * ENCAPSULAMENTO: atributos privados; estado só muda por métodos que validam as regras.
 * ABSTRAÇÃO/POLIMORFISMO: valores de diária, seguro e manutenção são definidos por cada categoria.
 */
public abstract class Veiculo {
    private static final Locale BR = Locale.forLanguageTag("pt-BR");

    private final String placa;
    private String modelo;
    private int ano;
    private double quilometragem;
    private StatusVeiculo status = StatusVeiculo.DISPONIVEL;

    protected Veiculo(String placa, String modelo, int ano, double quilometragem) throws DadosInvalidosException {
        this.placa = Validacao.placa(placa);
        setModelo(modelo);
        setAno(ano);
        this.quilometragem = Validacao.naoNegativo(quilometragem, "Quilometragem");
    }

    // ---- pontos de variação (polimorfismo) ----
    public abstract String getCategoria();
    public abstract double getDiariaBase();
    /** Percentual do seguro sobre a diária (ex.: 0.05 = 5%). */
    protected abstract double getTaxaSeguro();
    /** Custo de uma revisão/manutenção para a categoria. */
    public abstract double getCustoManutencao();

    // ---- cálculos comuns, baseados nos pontos de variação ----
    public double calcularDiarias(int dias) { return getDiariaBase() * dias; }
    public double calcularSeguro(int dias) { return getDiariaBase() * getTaxaSeguro() * dias; }
    /** Multa por atraso: 150% da diária por dia de atraso. */
    public double calcularMultaAtraso(int diasAtraso) { return getDiariaBase() * 1.5 * diasAtraso; }

    // ---- regras de estado ----
    public final void alugar() throws VeiculoIndisponivelException {
        if (status != StatusVeiculo.DISPONIVEL) throw new VeiculoIndisponivelException(placa, status.name());
        status = StatusVeiculo.ALUGADO;
    }

    public final void devolver(double kmChegada) throws DadosInvalidosException {
        if (status != StatusVeiculo.ALUGADO) throw new DadosInvalidosException("O veículo " + placa + " não está alugado.");
        if (kmChegada < quilometragem)
            throw new DadosInvalidosException("Quilometragem de chegada (" + kmChegada
                    + ") menor que a atual do veículo (" + quilometragem + ").");
        quilometragem = kmChegada;
        status = StatusVeiculo.DISPONIVEL;
    }

    public final void enviarParaManutencao() throws VeiculoIndisponivelException {
        if (status != StatusVeiculo.DISPONIVEL) throw new VeiculoIndisponivelException(placa, status.name());
        status = StatusVeiculo.MANUTENCAO;
    }

    public final void concluirManutencao() throws DadosInvalidosException {
        if (status != StatusVeiculo.MANUTENCAO)
            throw new DadosInvalidosException("O veículo " + placa + " não está em manutenção.");
        status = StatusVeiculo.DISPONIVEL;
    }

    // ---- getters / setters validados ----
    public String getPlaca() { return placa; }
    public String getModelo() { return modelo; }
    public int getAno() { return ano; }
    public double getQuilometragem() { return quilometragem; }
    public StatusVeiculo getStatus() { return status; }

    public final void setModelo(String modelo) throws DadosInvalidosException {
        this.modelo = Validacao.texto(modelo, "Modelo");
    }

    public final void setAno(int ano) throws DadosInvalidosException {
        int max = Year.now().getValue() + 1;
        if (ano < 1990 || ano > max)
            throw new DadosInvalidosException("Ano inválido: " + ano + " (entre 1990 e " + max + ").");
        this.ano = ano;
    }

    // ---- fábrica e persistência ----
    public static Veiculo criar(String categoria, String placa, String modelo, int ano, double km)
            throws DadosInvalidosException {
        if (categoria == null) throw new DadosInvalidosException("Categoria é obrigatória.");
        return switch (categoria.trim().toUpperCase()) {
            case "POPULAR" -> new Popular(placa, modelo, ano, km);
            case "SEDAN" -> new Sedan(placa, modelo, ano, km);
            case "SUV" -> new SUV(placa, modelo, ano, km);
            default -> throw new DadosInvalidosException("Categoria desconhecida: '" + categoria
                    + "' (use POPULAR, SEDAN ou SUV).");
        };
    }

    public String toLinha() {
        return String.join("|", getCategoria(), placa, modelo, String.valueOf(ano),
                String.valueOf(quilometragem), status.name());
    }

    public static Veiculo fromLinha(String linha) throws DadosInvalidosException {
        String[] p = linha.split("\\|", -1);
        if (p.length != 6) throw new DadosInvalidosException("Linha de veículo corrompida: " + linha);
        try {
            Veiculo v = criar(p[0], p[1], p[2], Integer.parseInt(p[3]), Double.parseDouble(p[4]));
            v.status = StatusVeiculo.valueOf(p[5]);
            return v;
        } catch (IllegalArgumentException e) {
            throw new DadosInvalidosException("Linha de veículo corrompida: " + linha);
        }
    }

    @Override
    public String toString() {
        return String.format(BR, "%-8s %-8s %-20s %d  %,10.0f km  %-10s diária R$ %.2f",
                getCategoria(), placa, modelo, ano, quilometragem, status, getDiariaBase());
    }
}
