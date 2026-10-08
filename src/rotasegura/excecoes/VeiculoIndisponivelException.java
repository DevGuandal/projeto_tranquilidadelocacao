package rotasegura.excecoes;

/** Tentativa de locar/enviar à manutenção um veículo ocupado ou indisponível. */
public class VeiculoIndisponivelException extends RotaSeguraException {
    public VeiculoIndisponivelException(String placa, String situacao) {
        super("Veículo " + placa + " indisponível (situação atual: " + situacao + ").");
    }
}
