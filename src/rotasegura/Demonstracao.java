package rotasegura;

import java.time.LocalDate;

import rotasegura.modelo.Locacao;
import rotasegura.servico.LocadoraService;

/** Roteiro automático para a apresentação: fluxo completo + cenários de erro tratados. */
public final class Demonstracao {
    private Demonstracao() { }

    @FunctionalInterface
    private interface Passo { void executar() throws Exception; }

    private static void tentar(String titulo, Passo passo) {
        System.out.println("\n>>> " + titulo);
        try {
            passo.executar();
        } catch (Exception e) {
            System.out.println("    [EXCEÇÃO TRATADA] " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public static void executar() {
        LocadoraService s = new LocadoraService(null); // sem gravar em disco
        LocalDate hoje = LocalDate.now();
        Locacao[] contrato = new Locacao[1];

        System.out.println("########## PARTE 1 - FLUXO COMPLETO ##########");
        tentar("Cadastrar frota", () -> {
            s.cadastrarVeiculo("POPULAR", "ABC1D23", "Fiat Mobi", 2023, 12000);
            s.cadastrarVeiculo("SEDAN", "DEF4567", "Honda Civic", 2022, 30500);
            s.cadastrarVeiculo("SUV", "GHI8J90", "Jeep Compass", 2024, 5000);
            s.listarVeiculos().forEach(System.out::println);
        });
        tentar("Cadastrar cliente", () -> {
            s.cadastrarCliente("529.982.247-25", "Maria Souza", "(14) 99999-1234", "maria@email.com");
            s.listarClientes().forEach(System.out::println);
        });
        tentar("Abrir locação (3 diárias, SEDAN)", () -> {
            contrato[0] = s.abrirLocacao("52998224725", "DEF4567", hoje, hoje.plusDays(3));
            contrato[0].imprimir();
        });
        tentar("Devolver com 2 dias de atraso e emitir comprovante", () -> {
            s.devolver(contrato[0].getId(), hoje.plusDays(5), 30900);
            contrato[0].imprimir();
        });
        tentar("Relatório de fechamento", () -> s.gerarRelatorio().imprimir());

        System.out.println("\n########## PARTE 2 - RESILIÊNCIA ##########");
        tentar("Alugar veículo ocupado", () -> {
            s.abrirLocacao("52998224725", "GHI8J90", hoje, hoje.plusDays(2));
            s.abrirLocacao("52998224725", "GHI8J90", hoje, hoje.plusDays(2));
        });
        tentar("Datas inválidas: fim antes do início", () ->
                s.abrirLocacao("52998224725", "ABC1D23", hoje.plusDays(5), hoje.plusDays(2)));
        tentar("Datas inválidas: início no passado", () ->
                s.abrirLocacao("52998224725", "ABC1D23", hoje.minusDays(3), hoje.plusDays(2)));
        tentar("Veículo em manutenção não pode ser alugado", () -> {
            System.out.printf("    custo da manutenção (POPULAR): R$ %.2f%n", s.enviarParaManutencao("ABC1D23"));
            s.abrirLocacao("52998224725", "ABC1D23", hoje, hoje.plusDays(2));
        });
        tentar("CPF inválido", () -> s.cadastrarCliente("111.111.111-11", "Fulano de Tal", "14988887777", "f@x.com"));
        tentar("Placa inválida", () -> s.cadastrarVeiculo("SUV", "12-ABC", "Qualquer", 2023, 0));
        tentar("Categoria inexistente", () -> s.cadastrarVeiculo("MOTO", "JKL1M23", "Honda CG", 2023, 0));
        tentar("Cliente inexistente", () -> s.abrirLocacao("168.995.350-09", "DEF4567", hoje, hoje.plusDays(1)));
        tentar("Devolver contrato já encerrado", () -> s.devolver(contrato[0].getId(), hoje, 31000));
        tentar("Km de chegada menor que o de saída", () -> {
            Locacao l = s.abrirLocacao("52998224725", "DEF4567", hoje, hoje.plusDays(1));
            s.devolver(l.getId(), hoje, 100);
        });
    }
}
