package br.pucminas.hospital.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.StatusProcedimento;

class ProcedimentoCirurgicoTest {

    private final Paciente paciente = new Paciente("Ana");
    private final Cirurgiao cirurgiao = new Cirurgiao("Dr. Bisturi", "CRM-MG 20001", "Cardiaca");
    private final ProfissionalSaude clinico = new ClinicoGeral("Dr. X", "CRM-MG 10001");
    private final LocalDateTime horario = LocalDateTime.of(2026, 6, 1, 9, 0);

    private ProcedimentoCirurgico novo() {
        return new ProcedimentoCirurgico(paciente, cirurgiao, "Apendicectomia", "Videolaparoscopia", horario, "Jejum de 8h");
    }

    @Test
    void nasceAgendado() {
        ProcedimentoCirurgico p = novo();
        assertEquals(StatusProcedimento.AGENDADO, p.getStatus());
        assertEquals(horario, p.getDataReferencia());
    }

    @Test
    void guardaTipoEObservacoesPreOperatorias() {
        ProcedimentoCirurgico p = novo();
        assertEquals("Videolaparoscopia", p.getTipoCirurgia());
        assertEquals("Jejum de 8h", p.getObservacoesPreOperatorias());
        assertEquals(horario, p.getDataHoraPrevista());
        assertNull(p.getObservacoesPosOperatorias());
        assertNull(p.getDataHoraInicio());
        assertNull(p.getDataHoraFim());
    }

    @Test
    void preOperatoriasSaoOpcionais() {
        ProcedimentoCirurgico p = new ProcedimentoCirurgico(paciente, cirurgiao, "Apendicectomia", "Aberta", horario,
                null);
        assertNull(p.getObservacoesPreOperatorias());
    }

    @Test
    void tipoDeCirurgiaEObrigatorio() {
        assertThrows(RegraNegocioException.class,
                () -> new ProcedimentoCirurgico(paciente, cirurgiao, "Apendicectomia", null, horario, null));
        assertThrows(RegraNegocioException.class,
                () -> new ProcedimentoCirurgico(paciente, cirurgiao, "Apendicectomia", "  ", horario, null));
    }

    @Test
    void concluirGuardaPosOperatoriasEHorarioEfetivo() {
        ProcedimentoCirurgico p = novo();
        p.iniciar(horario.plusMinutes(10));
        p.concluir("Recuperacao sem intercorrencias", horario.plusHours(2));

        assertEquals("Recuperacao sem intercorrencias", p.getObservacoesPosOperatorias());
        assertEquals(horario.plusMinutes(10), p.getDataHoraInicio());
        assertEquals(horario.plusHours(2), p.getDataHoraFim());
        assertEquals(horario, p.getDataHoraPrevista());
    }

    @Test
    void naoConcluiSemIniciarNaoGravaPosOperatorias() {
        ProcedimentoCirurgico p = novo();
        assertThrows(RegraNegocioException.class, () -> p.concluir("x", horario));
        assertNull(p.getObservacoesPosOperatorias());
    }

    @Test
    void responsavelPrecisaSerCirurgiao() {
        assertThrows(RegraNegocioException.class,
                () -> new ProcedimentoCirurgico(paciente, clinico, "Apendicectomia", "Videolaparoscopia", horario, null));
    }

    @Test
    void naoConcluiSemIniciar() {
        ProcedimentoCirurgico p = novo();
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> p.concluir("ok", horario));
        assertTrue(erro.getMessage().contains("iniciado"));
        assertEquals(StatusProcedimento.AGENDADO, p.getStatus());
        assertNull(p.getObservacoes());
    }

    @Test
    void fluxoIniciarEConcluir() {
        ProcedimentoCirurgico p = novo();
        p.iniciar(horario);
        assertEquals(StatusProcedimento.EM_ANDAMENTO, p.getStatus());
        p.concluir("Sem intercorrencias", horario.plusHours(2));
        assertEquals(StatusProcedimento.CONCLUIDO, p.getStatus());
        assertEquals("Sem intercorrencias", p.getObservacoes());
        assertEquals(horario.plusHours(2), p.getDataHoraFim());
    }

    @Test
    void naoConcluiDuasVezes() {
        ProcedimentoCirurgico p = novo();
        p.iniciar(horario);
        p.concluir(null, horario.plusHours(1));
        assertThrows(RegraNegocioException.class, () -> p.concluir(null, horario.plusHours(2)));
    }

    @Test
    void canceladoNaoPodeSerIniciado() {
        ProcedimentoCirurgico p = novo();
        p.cancelar();
        assertEquals(StatusProcedimento.CANCELADO, p.getStatus());
        assertThrows(RegraNegocioException.class, () -> p.iniciar(horario));
    }

    @Test
    void emAndamentoNaoPodeSerCancelado() {
        ProcedimentoCirurgico p = novo();
        p.iniciar(horario);
        assertThrows(RegraNegocioException.class, p::cancelar);
    }

    @Test
    void transicoesDoEnum() {
        assertFalse(StatusProcedimento.AGENDADO.podeIrPara(StatusProcedimento.CONCLUIDO));
        assertTrue(StatusProcedimento.AGENDADO.podeIrPara(StatusProcedimento.EM_ANDAMENTO));
        assertTrue(StatusProcedimento.EM_ANDAMENTO.podeIrPara(StatusProcedimento.CONCLUIDO));
        assertTrue(StatusProcedimento.CONCLUIDO.isFinal());
        assertTrue(StatusProcedimento.CANCELADO.isFinal());
    }
}
