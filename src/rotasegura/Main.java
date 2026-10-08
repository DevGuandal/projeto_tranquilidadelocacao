package rotasegura;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;

import rotasegura.excecoes.RotaSeguraException;
import rotasegura.modelo.Locacao;
import rotasegura.servico.LocadoraService;

/** Interface de console da Rota Segura. Todas as falhas de negócio chegam aqui como exceções tratadas. */
public class Main {
    private static final Scanner IN = new Scanner(System.in);
    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static void main(String[] args) {
        LocadoraService servico = new LocadoraService(Path.of("dados"));
        try {
            servico.carregar();
        } catch (RotaSeguraException e) {
            System.out.println("Aviso ao carregar dados salvos: " + e.getMessage());
        }

        System.out.println("=== ROTA SEGURA - Sistema de Locação ===");
        boolean rodando = true;
        while (rodando && IN.hasNextLine()) {
            menu();
            String op = IN.nextLine().trim();
            try {
                switch (op) {
                    case "1" -> {
                        var v = servico.cadastrarVeiculo(ler("Categoria (POPULAR/SEDAN/SUV)"), ler("Placa"),
                                ler("Modelo"), Integer.parseInt(ler("Ano")), numero("Quilometragem"));
                        System.out.println("Cadastrado: " + v);
                    }
                    case "2" -> servico.listarVeiculos().forEach(System.out::println);
                    case "3" -> {
                        var c = servico.cadastrarCliente(ler("CPF"), ler("Nome"), ler("Telefone (com DDD)"), ler("E-mail"));
                        System.out.println("Cadastrado: " + c);
                    }
                    case "4" -> servico.listarClientes().forEach(System.out::println);
                    case "5" -> {
                        Locacao l = servico.abrirLocacao(ler("CPF do cliente"), ler("Placa"),
                                data("Início (dd/MM/aaaa)"), data("Devolução prevista (dd/MM/aaaa)"));
                        l.imprimir();
                    }
                    case "6" -> {
                        Locacao l = servico.devolver(Integer.parseInt(ler("Nº do contrato")),
                                data("Data da devolução (dd/MM/aaaa)"), numero("Km de chegada"));
                        l.imprimir();
                    }
                    case "7" -> servico.listarLocacoes().forEach(System.out::println);
                    case "8" -> servico.gerarRelatorio().imprimir();
                    case "9" -> manutencao(servico);
                    case "10" -> Demonstracao.executar();
                    case "0" -> rodando = false;
                    default -> System.out.println("Opção inválida.");
                }
            } catch (RotaSeguraException e) {
                System.out.println("ERRO: " + e.getMessage());
            } catch (DateTimeParseException e) {
                System.out.println("ERRO: data inválida. Use dd/MM/aaaa com uma data que exista.");
            } catch (NumberFormatException e) {
                System.out.println("ERRO: valor numérico inválido.");
            }
        }
        System.out.println("Dados salvos na pasta 'dados'. Até logo!");
    }

    private static void manutencao(LocadoraService servico) throws RotaSeguraException {
        String acao = ler("1 = enviar para manutenção, 2 = concluir manutenção");
        String placa = ler("Placa");
        if (acao.equals("1")) {
            System.out.printf("Enviado. Custo estimado da manutenção: R$ %.2f%n", servico.enviarParaManutencao(placa));
        } else if (acao.equals("2")) {
            servico.concluirManutencao(placa);
            System.out.println("Manutenção concluída; veículo disponível.");
        } else {
            System.out.println("Opção inválida.");
        }
    }

    private static void menu() {
        System.out.println("""

                1) Cadastrar veículo      2) Listar veículos
                3) Cadastrar cliente      4) Listar clientes
                5) Abrir locação          6) Devolver / fechar locação
                7) Listar locações        8) Relatório de fechamento
                9) Manutenção            10) Executar demonstração automática
                0) Sair""");
        System.out.print("Opção: ");
    }

    private static String ler(String rotulo) {
        System.out.print(rotulo + ": ");
        return IN.nextLine();
    }

    private static double numero(String rotulo) {
        return Double.parseDouble(ler(rotulo).trim().replace(',', '.'));
    }

    private static LocalDate data(String rotulo) {
        return LocalDate.parse(ler(rotulo).trim(), DATA);
    }
}
