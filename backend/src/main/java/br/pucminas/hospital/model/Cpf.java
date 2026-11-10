package br.pucminas.hospital.model;

import java.util.regex.Pattern;

/**
 * Regras do CPF: normalizacao (so os digitos), validacao pelos digitos verificadores e formatacao.
 * O paciente guarda o CPF apenas com os 11 digitos; a mascara so aparece na resposta da API.
 */
public final class Cpf {

    private static final int TAMANHO = 11;

    /** Aceita 12345678909 ou 123.456.789-09, e nada alem disso. */
    private static final Pattern FORMATO = Pattern.compile("^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})$");

    private Cpf() {
    }

    /** Remove tudo que nao for digito. Nulo continua nulo. */
    public static String normalizar(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    public static boolean valido(String valor) {
        if (valor == null || !FORMATO.matcher(valor.trim()).matches()) {
            return false;
        }
        String cpf = normalizar(valor);
        if (todosIguais(cpf)) {
            return false;
        }
        return digitoVerificador(cpf, 9) == cpf.charAt(9) - '0'
                && digitoVerificador(cpf, 10) == cpf.charAt(10) - '0';
    }

    /** 12345678909 vira 123.456.789-09. O que nao tiver 11 digitos volta como veio. */
    public static String formatar(String valor) {
        String cpf = normalizar(valor);
        if (cpf == null || cpf.length() != TAMANHO) {
            return valor;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    private static boolean todosIguais(String cpf) {
        return cpf.chars().distinct().count() == 1;
    }

    /** Digito verificador calculado sobre os 'quantidade' primeiros digitos (9 para o primeiro, 10 para o segundo). */
    private static int digitoVerificador(String cpf, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (cpf.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
