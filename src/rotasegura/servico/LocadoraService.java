package rotasegura.servico;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import rotasegura.excecoes.*;
import rotasegura.modelo.*;
import rotasegura.relatorio.RelatorioFechamento;
import rotasegura.repositorio.Persistencia;
import rotasegura.repositorio.Repositorio;
import rotasegura.repositorio.RepositorioMemoria;
import rotasegura.util.Validacao;

/** Regras de negócio da locadora. Usa repositórios genéricos e persiste em arquivos de texto. */
public class LocadoraService {
    private final Repositorio<Veiculo, String> frota = new RepositorioMemoria<>(Veiculo::getPlaca);
    private final Repositorio<Cliente, String> clientes = new RepositorioMemoria<>(Cliente::getCpf);
    private final Repositorio<Locacao, Integer> contratos = new RepositorioMemoria<>(Locacao::getId);
    private final Path pasta; // null = sem persistência (usado na demonstração)
    private int proximoId = 1;

    public LocadoraService(Path pasta) { this.pasta = pasta; }

    // ---------- cadastros ----------
    public Veiculo cadastrarVeiculo(String categoria, String placa, String modelo, int ano, double km)
            throws RotaSeguraException {
        Veiculo v = Veiculo.criar(categoria, placa, modelo, ano, km);
        if (frota.existe(v.getPlaca())) throw new DadosInvalidosException("Já existe veículo com a placa " + v.getPlaca() + ".");
        frota.salvar(v);
        persistir();
        return v;
    }

    public Cliente cadastrarCliente(String cpf, String nome, String telefone, String email) throws RotaSeguraException {
        Cliente c = new Cliente(cpf, nome, telefone, email);
        if (clientes.existe(c.getCpf())) throw new DadosInvalidosException("Já existe cliente com o CPF " + c.getCpfFormatado() + ".");
        clientes.salvar(c);
        persistir();
        return c;
    }

    // ---------- locação ----------
    public Locacao abrirLocacao(String cpf, String placa, LocalDate inicio, LocalDate fim) throws RotaSeguraException {
        Cliente c = buscarCliente(cpf);
        Veiculo v = buscarVeiculo(placa);
        Locacao l = Locacao.abrir(proximoId, c, v, inicio, fim);
        proximoId++;
        contratos.salvar(l);
        persistir();
        return l;
    }

    public Locacao devolver(int idContrato, LocalDate dataDevolucao, double kmChegada) throws RotaSeguraException {
        Locacao l = contratos.buscarPorId(idContrato)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Contrato", idContrato));
        l.encerrar(dataDevolucao, kmChegada);
        persistir();
        return l;
    }

    // ---------- manutenção ----------
    /** Retorna o custo da manutenção (polimórfico, depende da categoria). */
    public double enviarParaManutencao(String placa) throws RotaSeguraException {
        Veiculo v = buscarVeiculo(placa);
        v.enviarParaManutencao();
        persistir();
        return v.getCustoManutencao();
    }

    public void concluirManutencao(String placa) throws RotaSeguraException {
        buscarVeiculo(placa).concluirManutencao();
        persistir();
    }

    // ---------- consultas ----------
    public Veiculo buscarVeiculo(String placa) throws RotaSeguraException {
        String chave = Validacao.placa(placa);
        return frota.buscarPorId(chave).orElseThrow(() -> new EntidadeNaoEncontradaException("Veículo", chave));
    }

    public Cliente buscarCliente(String cpf) throws RotaSeguraException {
        String chave = Validacao.cpf(cpf);
        return clientes.buscarPorId(chave).orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente", chave));
    }

    public List<Veiculo> listarVeiculos() { return frota.listar(); }
    public List<Cliente> listarClientes() { return clientes.listar(); }
    public List<Locacao> listarLocacoes() { return contratos.listar(); }

    public RelatorioFechamento gerarRelatorio() {
        return new RelatorioFechamento(contratos.listar(), frota.listar());
    }

    // ---------- persistência ----------
    private void persistir() throws PersistenciaException {
        if (pasta == null) return;
        try {
            Files.createDirectories(pasta);
            Persistencia.gravar(pasta.resolve("veiculos.txt"), frota.listar(), Veiculo::toLinha);
            Persistencia.gravar(pasta.resolve("clientes.txt"), clientes.listar(), Cliente::toLinha);
            Persistencia.gravar(pasta.resolve("locacoes.txt"), contratos.listar(), Locacao::toLinha);
        } catch (IOException e) {
            throw new PersistenciaException("Falha ao gravar os dados: " + e.getMessage(), e);
        }
    }

    public void carregar() throws RotaSeguraException {
        if (pasta == null) return;
        try {
            for (Veiculo v : Persistencia.ler(pasta.resolve("veiculos.txt"), Veiculo::fromLinha)) frota.salvar(v);
            for (Cliente c : Persistencia.ler(pasta.resolve("clientes.txt"), Cliente::fromLinha)) clientes.salvar(c);
            for (String linha : Persistencia.lerLinhas(pasta.resolve("locacoes.txt"))) {
                String[] p = linha.split("\\|", -1);
                if (p.length != 12) throw new DadosInvalidosException("Linha de locação corrompida: " + linha);
                Locacao l = Locacao.restaurar(p, buscarCliente(p[1]), buscarVeiculo(p[2]));
                contratos.salvar(l);
                proximoId = Math.max(proximoId, l.getId() + 1);
            }
        } catch (IOException e) {
            throw new PersistenciaException("Falha ao ler os dados: " + e.getMessage(), e);
        }
    }
}
