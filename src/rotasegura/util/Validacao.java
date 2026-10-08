package rotasegura.util;

import rotasegura.excecoes.DadosInvalidosException;

/** Regras de validação reutilizáveis (usadas pelos setters e construtores das entidades). */
public final class Validacao {
    private Validacao() { }

    public static String texto(String valor, String campo) throws DadosInvalidosException {
        if (valor == null || valor.isBlank())
            throw new DadosInvalidosException(campo + " é obrigatório.");
        if (valor.contains("|"))
            throw new DadosInvalidosException(campo + " não pode conter o caractere '|'.");
        return valor.trim();
    }

    /** Aceita padrão antigo (ABC1234) e Mercosul (ABC1D23). Retorna normalizada em maiúsculas. */
    public static String placa(String valor) throws DadosInvalidosException {
        String p = texto(valor, "Placa").toUpperCase().replace("-", "");
        if (!p.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}"))
            throw new DadosInvalidosException("Placa inválida: '" + valor + "' (use ABC1234 ou ABC1D23).");
        return p;
    }

    public static double naoNegativo(double valor, String campo) throws DadosInvalidosException {
        if (Double.isNaN(valor) || valor < 0)
            throw new DadosInvalidosException(campo + " não pode ser negativo(a).");
        return valor;
    }

    /** Valida CPF com dígitos verificadores. Retorna somente os 11 dígitos. */
    public static String cpf(String valor) throws DadosInvalidosException {
        String c = texto(valor, "CPF").replaceAll("[.\\-\\s]", "");
        if (!c.matches("\\d{11}") || c.chars().distinct().count() == 1
                || digito(c, 9) != c.charAt(9) - '0' || digito(c, 10) != c.charAt(10) - '0')
            throw new DadosInvalidosException("CPF inválido: '" + valor + "'.");
        return c;
    }

    private static int digito(String c, int n) {
        int soma = 0;
        for (int i = 0; i < n; i++) soma += (c.charAt(i) - '0') * (n + 1 - i);
        int r = (soma * 10) % 11;
        return r == 10 ? 0 : r;
    }

    public static String email(String valor) throws DadosInvalidosException {
        String e = texto(valor, "E-mail");
        if (!e.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$"))
            throw new DadosInvalidosException("E-mail inválido: '" + valor + "'.");
        return e;
    }

    public static String telefone(String valor) throws DadosInvalidosException {
        String t = texto(valor, "Telefone").replaceAll("[()\\-\\s]", "");
        if (!t.matches("\\d{10,11}"))
            throw new DadosInvalidosException("Telefone inválido: '" + valor + "' (informe DDD + número).");
        return t;
    }
}
