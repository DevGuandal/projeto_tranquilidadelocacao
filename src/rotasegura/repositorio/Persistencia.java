package rotasegura.repositorio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import rotasegura.excecoes.RotaSeguraException;

/** Gravação/leitura genérica em arquivo de texto (uma entidade por linha, campos separados por '|'). */
public final class Persistencia {
    private Persistencia() { }

    /** Converte uma linha de texto em objeto do tipo T. */
    @FunctionalInterface
    public interface Conversor<T> {
        T converter(String linha) throws RotaSeguraException;
    }

    public static <T> void gravar(Path arquivo, Collection<T> itens, Function<T, String> serializador)
            throws IOException {
        List<String> linhas = new ArrayList<>();
        for (T item : itens) linhas.add(serializador.apply(item));
        Files.write(arquivo, linhas);
    }

    public static List<String> lerLinhas(Path arquivo) throws IOException {
        if (!Files.exists(arquivo)) return List.of();
        return Files.readAllLines(arquivo).stream().filter(l -> !l.isBlank()).toList();
    }

    public static <T> List<T> ler(Path arquivo, Conversor<T> conversor) throws IOException, RotaSeguraException {
        List<T> itens = new ArrayList<>();
        for (String linha : lerLinhas(arquivo)) itens.add(conversor.converter(linha));
        return itens;
    }
}
