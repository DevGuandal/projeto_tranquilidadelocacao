package rotasegura.modelo;

import rotasegura.excecoes.DadosInvalidosException;
import rotasegura.util.Validacao;

/** Cliente da locadora. Dados privados; todo setter valida antes de gravar. */
public class Cliente {
    private final String cpf;
    private String nome;
    private String telefone;
    private String email;

    public Cliente(String cpf, String nome, String telefone, String email) throws DadosInvalidosException {
        this.cpf = Validacao.cpf(cpf);
        setNome(nome);
        setTelefone(telefone);
        setEmail(email);
    }

    public String getCpf() { return cpf; }
    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }

    public String getCpfFormatado() {
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    public final void setNome(String nome) throws DadosInvalidosException {
        String n = Validacao.texto(nome, "Nome");
        if (n.length() < 3) throw new DadosInvalidosException("Nome muito curto.");
        this.nome = n;
    }
    public final void setTelefone(String telefone) throws DadosInvalidosException { this.telefone = Validacao.telefone(telefone); }
    public final void setEmail(String email) throws DadosInvalidosException { this.email = Validacao.email(email); }

    public String toLinha() { return String.join("|", cpf, nome, telefone, email); }

    public static Cliente fromLinha(String linha) throws DadosInvalidosException {
        String[] p = linha.split("\\|", -1);
        if (p.length != 4) throw new DadosInvalidosException("Linha de cliente corrompida: " + linha);
        return new Cliente(p[0], p[1], p[2], p[3]);
    }

    @Override
    public String toString() { return String.format("%-14s %-25s %-12s %s", getCpfFormatado(), nome, telefone, email); }
}
