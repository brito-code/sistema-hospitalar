package br.pucminas.hospital.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import br.pucminas.hospital.exception.RegraNegocioException;

class PacienteTest {

    private static final LocalDate NASCIMENTO = LocalDate.of(1988, 3, 14);

    private Endereco endereco() {
        return new Endereco("Rua da Bahia", "1200", "Apto 301", "Centro", "Belo Horizonte", "mg", "30160-011");
    }

    private Paciente paciente() {
        return new Paciente("  Ana Clara Mendes ", "123.456.789-09", NASCIMENTO, "(31) 98812-3401",
                " Ana.Mendes@Email.com ", endereco());
    }

    @Test
    void guardaOsDadosNormalizados() {
        Paciente p = paciente();

        assertEquals("Ana Clara Mendes", p.getNome());
        assertEquals("12345678909", p.getCpf());
        assertEquals("ana.mendes@email.com", p.getEmail());
        assertEquals("(31) 98812-3401", p.getTelefone());
        assertEquals(NASCIMENTO, p.getDataNascimento());
    }

    @Test
    void enderecoNormalizaUfECep() {
        Endereco e = endereco();

        assertEquals("MG", e.getUf());
        assertEquals("30160011", e.getCep());
        assertEquals("30160-011", e.getCepFormatado());
        assertEquals("Apto 301", e.getComplemento());
        assertNull(new Endereco("Rua A", "1", "  ", "Centro", "BH", "MG", "30160011").getComplemento());
    }

    @Test
    void calculaIdadeEmAnosCompletos() {
        Paciente p = paciente();

        assertEquals(37, p.calcularIdade(LocalDate.of(2026, 3, 13)));
        assertEquals(38, p.calcularIdade(LocalDate.of(2026, 3, 14)));
        assertEquals(38, p.calcularIdade(LocalDate.of(2026, 12, 31)));
        assertEquals(0, p.calcularIdade(NASCIMENTO.plusMonths(6)));
    }

    @Test
    void idadeFicaNulaSemDataDeNascimento() {
        Paciente semDados = new Paciente("Ana");

        assertNull(semDados.getIdade());
        assertNull(semDados.getCpf());
        assertNull(semDados.getEndereco());
    }

    @Test
    void naoAceitaCpfInvalido() {
        assertThrows(RegraNegocioException.class, () -> new Paciente("Ana", "111.111.111-11", NASCIMENTO,
                "(31) 98812-3401", "ana@email.com", endereco()));
        assertThrows(RegraNegocioException.class, () -> new Paciente("Ana", null, NASCIMENTO,
                "(31) 98812-3401", "ana@email.com", endereco()));
    }

    @Test
    void naoAceitaDataDeNascimentoFuturaOuAusente() {
        LocalDate amanha = LocalDate.now().plusDays(1);

        assertThrows(RegraNegocioException.class, () -> new Paciente("Ana", "12345678909", amanha,
                "(31) 98812-3401", "ana@email.com", endereco()));
        assertThrows(RegraNegocioException.class, () -> new Paciente("Ana", "12345678909", null,
                "(31) 98812-3401", "ana@email.com", endereco()));
    }

    @Test
    void atualizarDadosAplicaAsMesmasRegrasENaoAlteraNadaSeFalhar() {
        Paciente p = paciente();

        p.atualizarDados("Ana C. Mendes", "529.982.247-25", LocalDate.of(1990, 1, 2), "(31) 3331-7780",
                "nova@email.com", endereco());
        assertEquals("Ana C. Mendes", p.getNome());
        assertEquals("52998224725", p.getCpf());
        assertEquals(LocalDate.of(1990, 1, 2), p.getDataNascimento());

        assertThrows(RegraNegocioException.class, () -> p.atualizarDados("Outro Nome", "123", NASCIMENTO,
                "(31) 3331-7780", "outro@email.com", endereco()));
        assertEquals("Ana C. Mendes", p.getNome());
        assertEquals("52998224725", p.getCpf());
    }

    @Test
    void historicoComecaVazio() {
        Paciente p = paciente();

        assertTrue(p.getAtendimentos().isEmpty());
        assertTrue(p.getConsultas().isEmpty());
        assertTrue(p.getInternacoes().isEmpty());
    }
}
