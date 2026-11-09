package br.pucminas.hospital.model;

import static br.pucminas.hospital.model.enums.StatusConsulta.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import br.pucminas.hospital.model.enums.StatusConsulta;

class StatusConsultaTest {

    @Test
    void transicoesValidas() {
        assertTrue(AGENDADA.podeIrPara(CONFIRMADA));
        assertTrue(AGENDADA.podeIrPara(CANCELADA));
        assertTrue(AGENDADA.podeIrPara(NAO_COMPARECEU));
        assertTrue(CONFIRMADA.podeIrPara(EM_ATENDIMENTO));
        assertTrue(CONFIRMADA.podeIrPara(CANCELADA));
        assertTrue(CONFIRMADA.podeIrPara(NAO_COMPARECEU));
        assertTrue(EM_ATENDIMENTO.podeIrPara(CONCLUIDA));
    }

    @Test
    void transicoesIncompativeisSaoBloqueadas() {
        assertFalse(AGENDADA.podeIrPara(CONCLUIDA));
        assertFalse(AGENDADA.podeIrPara(EM_ATENDIMENTO));
        assertFalse(CONFIRMADA.podeIrPara(AGENDADA));
        assertFalse(CONFIRMADA.podeIrPara(CONCLUIDA));
        assertFalse(EM_ATENDIMENTO.podeIrPara(CANCELADA));
        assertFalse(EM_ATENDIMENTO.podeIrPara(CONFIRMADA));
        assertFalse(EM_ATENDIMENTO.podeIrPara(NAO_COMPARECEU));
        assertFalse(EM_ATENDIMENTO.podeIrPara(AGENDADA));
        assertFalse(AGENDADA.podeIrPara(null));
    }

    @Test
    void estadosFinaisNaoTransitam() {
        for (StatusConsulta final_ : new StatusConsulta[] { CONCLUIDA, CANCELADA, NAO_COMPARECEU }) {
            assertTrue(final_.isFinal());
            for (StatusConsulta destino : StatusConsulta.values()) {
                assertFalse(final_.podeIrPara(destino), final_ + " -> " + destino);
            }
        }
    }
}
