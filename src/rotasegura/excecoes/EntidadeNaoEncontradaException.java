package rotasegura.excecoes;

/** Cliente, veículo ou contrato inexistente. */
public class EntidadeNaoEncontradaException extends RotaSeguraException {
    public EntidadeNaoEncontradaException(String tipo, Object chave) {
        super(tipo + " não encontrado(a): " + chave);
    }
}
