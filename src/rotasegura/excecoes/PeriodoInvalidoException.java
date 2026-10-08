package rotasegura.excecoes;

/** Datas inconsistentes (fim antes do início, início no passado, devolução antes da retirada). */
public class PeriodoInvalidoException extends RotaSeguraException {
    public PeriodoInvalidoException(String mensagem) { super(mensagem); }
}
