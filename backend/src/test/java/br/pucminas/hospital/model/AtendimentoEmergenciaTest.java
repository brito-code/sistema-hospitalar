package br.pucminas.hospital.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.Prioridade;
import br.pucminas.hospital.model.enums.StatusEmergencia;

class AtendimentoEmergenciaTest {

    private final Paciente paciente = new Paciente("Ana");
    private final ProfissionalSaude medico = new ClinicoGeral("Dr. X", "CRM-MG 10001");
    private final LocalDateTime chegada = LocalDateTime.of(2026, 5, 10, 14, 0);

    private AtendimentoEmergencia nova(Prioridade prioridade, LocalDateTime quando) {
        return new AtendimentoEmergencia(paciente, medico, prioridade, "Dor no peito", quando);
    }

    @Test
    void chegaAguardandoAvaliacao() {
        AtendimentoEmergencia e = nova(null, chegada);
        assertEquals(StatusEmergencia.AGUARDANDO_AVALIACAO, e.getStatus());
        assertTrue(e.isAguardando());
        assertEquals(chegada, e.getDataReferencia());
    }

    @Test
    void naoIniciaSemPrioridade() {
        AtendimentoEmergencia e = nova(null, chegada);
        assertThrows(RegraNegocioException.class, () -> e.iniciarAtendimento(chegada.plusMinutes(5)));
        assertEquals(StatusEmergencia.AGUARDANDO_AVALIACAO, e.getStatus());
    }

    @Test
    void fluxoCompletoComTriagem() {
        AtendimentoEmergencia e = nova(null, chegada);
        e.classificar(Prioridade.ALTA);
        e.iniciarAtendimento(chegada.plusMinutes(5));
        assertEquals(StatusEmergencia.EM_ATENDIMENTO, e.getStatus());
        e.concluir("Paciente estabilizado", chegada.plusHours(1));
        assertEquals(StatusEmergencia.CONCLUIDO, e.getStatus());
        assertEquals("Paciente estabilizado", e.getObservacoes());
        assertEquals(chegada.plusHours(1), e.getDataHoraFim());
    }

    @Test
    void naoConcluiSemIniciar() {
        AtendimentoEmergencia e = nova(Prioridade.BAIXA, chegada);
        assertThrows(RegraNegocioException.class, () -> e.concluir(null, chegada.plusHours(1)));
    }

    @Test
    void concluidoNaoReclassificaNemReinicia() {
        AtendimentoEmergencia e = nova(Prioridade.MODERADA, chegada);
        e.iniciarAtendimento(chegada);
        e.concluir(null, chegada.plusHours(1));
        assertThrows(RegraNegocioException.class, () -> e.classificar(Prioridade.CRITICA));
        assertThrows(RegraNegocioException.class, () -> e.iniciarAtendimento(chegada));
    }

    @Test
    void queixaEChegadaSaoObrigatorias() {
        assertThrows(RegraNegocioException.class,
                () -> new AtendimentoEmergencia(paciente, medico, Prioridade.ALTA, " ", chegada));
        assertThrows(RegraNegocioException.class,
                () -> new AtendimentoEmergencia(paciente, medico, Prioridade.ALTA, "Febre", null));
    }

    @Test
    void filaOrdenaPorPrioridadeEDepoisPorChegada() {
        AtendimentoEmergencia baixa = nova(Prioridade.BAIXA, chegada);
        AtendimentoEmergencia critica = nova(Prioridade.CRITICA, chegada.plusMinutes(30));
        AtendimentoEmergencia altaCedo = nova(Prioridade.ALTA, chegada.plusMinutes(1));
        AtendimentoEmergencia altaTarde = nova(Prioridade.ALTA, chegada.plusMinutes(20));
        AtendimentoEmergencia semTriagem = nova(null, chegada.minusHours(1));

        List<AtendimentoEmergencia> fila = List.of(baixa, semTriagem, altaTarde, critica, altaCedo).stream()
                .sorted(AtendimentoEmergencia.ORDEM_DA_FILA).toList();

        assertEquals(List.of(critica, altaCedo, altaTarde, baixa, semTriagem), fila);
    }

    @Test
    void transicoesDoEnum() {
        assertTrue(StatusEmergencia.AGUARDANDO_AVALIACAO.podeIrPara(StatusEmergencia.EM_ATENDIMENTO));
        assertFalse(StatusEmergencia.AGUARDANDO_AVALIACAO.podeIrPara(StatusEmergencia.CONCLUIDO));
        assertTrue(StatusEmergencia.EM_ATENDIMENTO.podeIrPara(StatusEmergencia.CONCLUIDO));
        assertTrue(StatusEmergencia.CONCLUIDO.isFinal());
        assertFalse(StatusEmergencia.EM_ATENDIMENTO.podeIrPara(null));
    }
}
