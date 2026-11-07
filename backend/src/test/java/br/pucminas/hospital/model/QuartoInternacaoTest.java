package br.pucminas.hospital.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import br.pucminas.hospital.exception.AltaInvalidaException;
import br.pucminas.hospital.exception.QuartoSemVagaException;
import br.pucminas.hospital.model.enums.SituacaoQuarto;
import br.pucminas.hospital.model.enums.TipoQuarto;

class QuartoInternacaoTest {

    private final Paciente paciente = new Paciente("Ana");
    private final ProfissionalSaude medico = new ProfissionalSaude("Dr. X");
    private final LocalDate entrada = LocalDate.of(2026, 5, 10);

    private Internacao internar(Quarto quarto) {
        Internacao i = new Internacao(paciente, medico, quarto, entrada, entrada.plusDays(5));
        i.realizar("obs");
        i.alocarNoQuarto();
        return i;
    }

    @Test
    void quartoComVagaAceitaInternacao() {
        Quarto q = new Quarto("101", 1, 2, TipoQuarto.ENFERMARIA);
        internar(q);
        assertEquals(1, q.getOcupacaoAtual());
        assertTrue(q.temVaga());
        assertEquals(SituacaoQuarto.DISPONIVEL, q.getSituacao());
    }

    @Test
    void quartoLotadoRejeitaInternacao() {
        Quarto q = new Quarto("102", 1, 1, TipoQuarto.UTI);
        internar(q);
        assertFalse(q.temVaga());
        assertEquals(SituacaoQuarto.OCUPADO, q.getSituacao());
        Internacao outra = new Internacao(paciente, medico, q, entrada, null);
        assertThrows(QuartoSemVagaException.class, () -> outra.realizar(null));
        assertEquals(1, q.getOcupacaoAtual());
    }

    @Test
    void altaValidaLiberaVaga() {
        Quarto q = new Quarto("103", 1, 1, TipoQuarto.APARTAMENTO);
        Internacao i = internar(q);
        i.darAlta(entrada.plusDays(3));
        assertFalse(i.isAtiva());
        assertEquals(3, i.getDiasInternado());
        assertEquals(0, q.getOcupacaoAtual());
        assertTrue(q.temVaga());
    }

    @Test
    void altaDuplicadaEhRejeitada() {
        Internacao i = internar(new Quarto("104", 1, 1, TipoQuarto.ENFERMARIA));
        i.darAlta(entrada);
        assertThrows(AltaInvalidaException.class, () -> i.darAlta(entrada.plusDays(1)));
    }

    @Test
    void altaAntesDaEntradaEhRejeitada() {
        Internacao i = internar(new Quarto("105", 1, 1, TipoQuarto.ENFERMARIA));
        assertThrows(AltaInvalidaException.class, () -> i.darAlta(entrada.minusDays(1)));
        assertTrue(i.isAtiva());
    }

    @Test
    void dataReferenciaEhAEntrada() {
        Internacao i = internar(new Quarto("106", 1, 1, TipoQuarto.ENFERMARIA));
        assertEquals(entrada.atStartOfDay(), i.getDataReferencia());
        assertEquals("obs", i.getObservacoes());
    }
}
