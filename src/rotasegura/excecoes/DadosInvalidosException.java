package rotasegura.excecoes;

/** Dado informado fora das regras (CPF, placa, km, campo vazio, linha corrompida...). */
public class DadosInvalidosException extends RotaSeguraException {
    public DadosInvalidosException(String mensagem) { super(mensagem); }
}
