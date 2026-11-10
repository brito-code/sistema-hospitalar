package br.pucminas.hospital.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import br.pucminas.hospital.GeradorDeCpf;

class CpfTest {

    @Test
    void aceitaCpfValidoComEmMascara() {
        assertTrue(Cpf.valido("12345678909"));
        assertTrue(Cpf.valido("123.456.789-09"));
        assertTrue(Cpf.valido("529.982.247-25"));
    }

    @Test
    void rejeitaDigitoVerificadorErrado() {
        assertFalse(Cpf.valido("12345678900"));
        assertFalse(Cpf.valido("123.456.789-19"));
    }

    @Test
    void rejeitaSequenciaDeDigitosIguais() {
        assertFalse(Cpf.valido("11111111111"));
        assertFalse(Cpf.valido("000.000.000-00"));
    }

    @Test
    void rejeitaTamanhoEFormatoInvalidos() {
        assertFalse(Cpf.valido(null));
        assertFalse(Cpf.valido(""));
        assertFalse(Cpf.valido("1234567890"));
        assertFalse(Cpf.valido("123456789091"));
        assertFalse(Cpf.valido("123.456.789/09"));
        assertFalse(Cpf.valido("12345678909x"));
    }

    @Test
    void normalizaEFormata() {
        assertEquals("12345678909", Cpf.normalizar("123.456.789-09"));
        assertEquals("123.456.789-09", Cpf.formatar("12345678909"));
        assertEquals("123.456.789-09", Cpf.formatar("123.456.789-09"));
        assertNull(Cpf.normalizar(null));
        assertNull(Cpf.formatar(null));
        assertEquals("123", Cpf.formatar("123"));
    }

    @Test
    void geradorDeTesteSoProduzCpfValidoEUnico() {
        String primeiro = GeradorDeCpf.proximo();
        String segundo = GeradorDeCpf.proximo();
        assertTrue(Cpf.valido(primeiro));
        assertTrue(Cpf.valido(segundo));
        assertNotEquals(primeiro, segundo);
    }
}
