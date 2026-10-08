package rotasegura.relatorio;

/**
 * Interface padronizada para qualquer documento do sistema (contrato, comprovante, relatório).
 * Polimorfismo: quem imprime não precisa saber o tipo concreto.
 */
public interface Imprimivel {
    String gerarDocumento();

    default void imprimir() {
        System.out.println(gerarDocumento());
    }
}
