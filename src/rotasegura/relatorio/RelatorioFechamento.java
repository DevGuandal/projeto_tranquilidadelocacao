package rotasegura.relatorio;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import rotasegura.modelo.Locacao;
import rotasegura.modelo.Veiculo;

/** Relatório de fechamento: faturamento por categoria e situação da frota. */
public class RelatorioFechamento implements Imprimivel {
    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private final List<Locacao> locacoes;
    private final List<Veiculo> frota;

    public RelatorioFechamento(List<Locacao> locacoes, List<Veiculo> frota) {
        this.locacoes = List.copyOf(locacoes);
        this.frota = List.copyOf(frota);
    }

    @Override
    public String gerarDocumento() {
        List<Locacao> fechadas = locacoes.stream().filter(Locacao::isFinalizada).toList();
        long abertas = locacoes.size() - fechadas.size();
        Map<String, Double> porCategoria = fechadas.stream().collect(Collectors.groupingBy(
                l -> l.getVeiculo().getCategoria(), TreeMap::new,
                Collectors.summingDouble(Locacao::getValorTotal)));
        Map<?, Long> situacao = frota.stream().collect(Collectors.groupingBy(
                Veiculo::getStatus, TreeMap::new, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append("==================== ROTA SEGURA - RELATÓRIO DE FECHAMENTO ====================\n");
        sb.append(String.format(BR, "Contratos finalizados: %d | Contratos em aberto: %d%n", fechadas.size(), abertas));
        sb.append("Faturamento por categoria:\n");
        if (porCategoria.isEmpty()) sb.append("  (nenhuma locação finalizada)\n");
        porCategoria.forEach((cat, valor) ->
                sb.append(String.format(BR, "  %-8s R$ %,10.2f%n", cat, valor)));
        double total = porCategoria.values().stream().mapToDouble(Double::doubleValue).sum();
        sb.append(String.format(BR, "FATURAMENTO TOTAL: R$ %,.2f%n", total));
        sb.append("Situação da frota: ").append(situacao).append('\n');
        sb.append("===============================================================================");
        return sb.toString();
    }
}
