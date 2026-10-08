package rotasegura.excecoes;

/** Exceção-base (checada) do domínio: obriga o chamador a tratar falhas de negócio. */
public class RotaSeguraException extends Exception {
    public RotaSeguraException(String mensagem) { super(mensagem); }
    public RotaSeguraException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
