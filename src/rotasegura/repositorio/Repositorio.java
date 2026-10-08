package rotasegura.repositorio;

import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico de repositório. T = tipo da entidade, ID = tipo da chave.
 * Reaproveitado para Veículo (String), Cliente (String) e Locação (Integer).
 */
public interface Repositorio<T, ID> {
    void salvar(T entidade);
    Optional<T> buscarPorId(ID id);
    boolean existe(ID id);
    List<T> listar();
}
