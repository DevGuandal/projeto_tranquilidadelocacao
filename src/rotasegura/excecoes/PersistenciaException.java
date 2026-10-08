package rotasegura.excecoes;

/** Falha de leitura/gravação dos arquivos de dados. */
public class PersistenciaException extends RotaSeguraException {
    public PersistenciaException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
