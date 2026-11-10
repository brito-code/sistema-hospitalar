package br.pucminas.hospital;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gera CPFs validos e diferentes a cada chamada. O banco de teste e compartilhado entre as classes de teste,
 * entao cada teste que cadastra paciente usa um CPF proprio em vez de limpar a tabela.
 */
public final class GeradorDeCpf {

    private static final AtomicInteger SEQUENCIA = new AtomicInteger(100000000);

    private GeradorDeCpf() {
    }

    /** CPF so com digitos, ex.: 10000000191. */
    public static String proximo() {
        String base = String.valueOf(SEQUENCIA.incrementAndGet());
        String comPrimeiro = base + digito(base);
        return comPrimeiro + digito(comPrimeiro);
    }

    private static int digito(String digitos) {
        int soma = 0;
        for (int i = 0; i < digitos.length(); i++) {
            soma += (digitos.charAt(i) - '0') * (digitos.length() + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
