package rotasegura.repositorio;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/** Implementação genérica em memória, baseada em Map (mantém a ordem de cadastro). */
public class RepositorioMemoria<T, ID> implements Repositorio<T, ID> {
    private final Map<ID, T> dados = new LinkedHashMap<>();
    private final Function<T, ID> extratorDeChave;

    public RepositorioMemoria(Function<T, ID> extratorDeChave) {
        this.extratorDeChave = extratorDeChave;
    }

    @Override public void salvar(T entidade) { dados.put(extratorDeChave.apply(entidade), entidade); }
    @Override public Optional<T> buscarPorId(ID id) { return Optional.ofNullable(dados.get(id)); }
    @Override public boolean existe(ID id) { return dados.containsKey(id); }
    /** Devolve cópia imutável: quem chama não consegue alterar a coleção interna. */
    @Override public List<T> listar() { return List.copyOf(dados.values()); }
}
