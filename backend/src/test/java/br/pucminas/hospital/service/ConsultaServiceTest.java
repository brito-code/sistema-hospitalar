package br.pucminas.hospital.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.pucminas.hospital.dto.ConsultaRequest;
import br.pucminas.hospital.dto.ConsultaResponse;
import br.pucminas.hospital.dto.ReagendarRequest;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.Consulta;
import br.pucminas.hospital.model.ClinicoGeral;
import br.pucminas.hospital.model.ConsultaEspecializada;
import br.pucminas.hospital.model.ConsultaRotina;
import br.pucminas.hospital.model.MedicoEspecialista;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.model.enums.StatusConsulta;
import br.pucminas.hospital.model.enums.TipoConsulta;
import br.pucminas.hospital.repository.ConsultaRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock ConsultaRepository consultas;
    @Mock PacienteRepository pacientes;
    @Mock ProfissionalSaudeRepository profissionais;
    @InjectMocks ConsultaService service;

    Paciente paciente;
    ProfissionalSaude clinico;
    final LocalDate data = LocalDate.of(2026, 10, 6);
    final LocalTime horario = LocalTime.of(8, 0);

    @BeforeEach
    void preparar() {
        paciente = new Paciente("Ana");
        ReflectionTestUtils.setField(paciente, "id", 1L);
        clinico = new ClinicoGeral("Dra. Marina", "CRM-MG 10002");
        ReflectionTestUtils.setField(clinico, "id", 2L);
        lenient().when(pacientes.findById(1L)).thenReturn(Optional.of(paciente));
        lenient().when(profissionais.findById(2L)).thenReturn(Optional.of(clinico));
        lenient().when(consultas.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void doisAtendimentosNoMesmoHorarioSaoBloqueados() {
        when(consultas.existeConflito(2L, data, horario)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> service.agendar(rotina()));
        verify(consultas, never()).save(any());
    }

    @Test
    void horarioLivreQuandoNaoHaConflito() {
        when(consultas.existeConflito(2L, data, horario)).thenReturn(false);

        ConsultaResponse criada = service.agendar(rotina());

        assertEquals(StatusConsulta.AGENDADA, criada.status());
        assertEquals(TipoConsulta.ROTINA, criada.tipo());
        verify(consultas).existeConflito(2L, data, horario);
        verify(consultas).save(any(Consulta.class));
    }

    @Test
    void consultaEspecializadaExigeEspecialidadeDoProfissional() {
        MedicoEspecialista cardiologista = new MedicoEspecialista("Dra. Mariana", "CRM-MG 45231", Especialidade.CARDIOLOGIA);
        ReflectionTestUtils.setField(cardiologista, "id", 2L);
        when(profissionais.findById(2L)).thenReturn(Optional.of(cardiologista));
        when(consultas.existeConflito(2L, data, horario)).thenReturn(false);

        ConsultaResponse criada = service.agendar(especializada(Especialidade.CARDIOLOGIA));
        assertEquals(TipoConsulta.ESPECIALIZADA, criada.tipo());
        assertEquals(Especialidade.CARDIOLOGIA, criada.especialidade());

        assertThrows(RegraNegocioException.class, () -> service.agendar(especializada(Especialidade.ORTOPEDIA)));
    }

    @Test
    void transicaoInvalidaERejeitada() {
        Consulta consulta = new ConsultaRotina(paciente, clinico, data, horario, "retorno", null);
        consulta.transitarPara(StatusConsulta.CANCELADA);
        when(consultas.findById(9L)).thenReturn(Optional.of(consulta));

        assertThrows(RegraNegocioException.class, () -> service.confirmar(9L));
        assertEquals(StatusConsulta.CANCELADA, consulta.getStatus());
    }

    @Test
    void reagendarCancelaAOriginalECriaOutraAgendada() {
        Consulta original = new ConsultaRotina(paciente, clinico, data, horario, "retorno", "jejum");
        when(consultas.findById(4L)).thenReturn(Optional.of(original));
        LocalDate novaData = data.plusDays(1);
        LocalTime novoHorario = LocalTime.of(9, 30);
        when(consultas.existeConflito(2L, novaData, novoHorario)).thenReturn(false);

        ConsultaResponse nova = service.reagendar(4L, new ReagendarRequest(novaData, novoHorario));

        assertEquals(StatusConsulta.CANCELADA, original.getStatus());
        assertEquals(StatusConsulta.AGENDADA, nova.status());
        assertEquals(novaData, nova.data());
        assertEquals(novoHorario, nova.horario());
        assertEquals("retorno", nova.motivo());
        assertEquals("jejum", nova.observacoesMedicas());
        verify(consultas).saveAndFlush(original);
    }

    @Test
    void fluxoCompletoGravaObservacoesNaConclusao() {
        Consulta consulta = new ConsultaRotina(paciente, clinico, data, horario, "retorno", "jejum");
        when(consultas.findById(5L)).thenReturn(Optional.of(consulta));

        assertEquals(StatusConsulta.CONFIRMADA, service.confirmar(5L).status());
        assertEquals(StatusConsulta.EM_ATENDIMENTO, service.iniciar(5L).status());
        ConsultaResponse concluida = service.concluir(5L, "Paciente estavel, retorno em 30 dias");

        assertEquals(StatusConsulta.CONCLUIDA, concluida.status());
        assertEquals("Paciente estavel, retorno em 30 dias", concluida.observacoesMedicas());
    }

    @Test
    void concluirSemIniciarFalhaEPreservaObservacoes() {
        Consulta consulta = new ConsultaRotina(paciente, clinico, data, horario, "retorno", "jejum");
        consulta.transitarPara(StatusConsulta.CONFIRMADA);
        when(consultas.findById(6L)).thenReturn(Optional.of(consulta));

        assertThrows(RegraNegocioException.class, () -> service.concluir(6L, "tentativa"));
        assertEquals(StatusConsulta.CONFIRMADA, consulta.getStatus());
        assertEquals("jejum", consulta.getObservacoes());
    }

    @Test
    void consultaCanceladaNaoPodeSerConcluidaNemIniciada() {
        Consulta consulta = new ConsultaRotina(paciente, clinico, data, horario, "retorno", null);
        consulta.transitarPara(StatusConsulta.CANCELADA);
        when(consultas.findById(7L)).thenReturn(Optional.of(consulta));

        assertThrows(RegraNegocioException.class, () -> service.concluir(7L, "obs"));
        assertThrows(RegraNegocioException.class, () -> service.iniciar(7L));
        assertEquals(StatusConsulta.CANCELADA, consulta.getStatus());
    }

    @Test
    void consultaConcluidaNaoVoltaParaAgendadaNemCancela() {
        Consulta consulta = new ConsultaRotina(paciente, clinico, data, horario, "retorno", null);
        consulta.transitarPara(StatusConsulta.CONFIRMADA);
        consulta.iniciarAtendimento();
        consulta.concluir("ok");
        when(consultas.findById(8L)).thenReturn(Optional.of(consulta));

        assertThrows(RegraNegocioException.class, () -> consulta.transitarPara(StatusConsulta.AGENDADA));
        assertThrows(RegraNegocioException.class, () -> service.cancelar(8L));
        assertThrows(RegraNegocioException.class, () -> service.naoCompareceu(8L));
        assertEquals(StatusConsulta.CONCLUIDA, consulta.getStatus());
    }

    @Test
    void naoComparecimentoSoAPartirDeAgendadaOuConfirmada() {
        Consulta agendada = new ConsultaRotina(paciente, clinico, data, horario, "a", null);
        Consulta confirmada = new ConsultaRotina(paciente, clinico, data, horario, "b", null);
        confirmada.transitarPara(StatusConsulta.CONFIRMADA);
        Consulta emAtendimento = new ConsultaRotina(paciente, clinico, data, horario, "c", null);
        emAtendimento.transitarPara(StatusConsulta.CONFIRMADA);
        emAtendimento.iniciarAtendimento();
        Consulta cancelada = new ConsultaRotina(paciente, clinico, data, horario, "d", null);
        cancelada.transitarPara(StatusConsulta.CANCELADA);
        when(consultas.findById(11L)).thenReturn(Optional.of(agendada));
        when(consultas.findById(12L)).thenReturn(Optional.of(confirmada));
        when(consultas.findById(13L)).thenReturn(Optional.of(emAtendimento));
        when(consultas.findById(14L)).thenReturn(Optional.of(cancelada));

        assertEquals(StatusConsulta.NAO_COMPARECEU, service.naoCompareceu(11L).status());
        assertEquals(StatusConsulta.NAO_COMPARECEU, service.naoCompareceu(12L).status());
        assertThrows(RegraNegocioException.class, () -> service.naoCompareceu(13L));
        assertThrows(RegraNegocioException.class, () -> service.naoCompareceu(14L));
    }

    @Test
    void iniciarExigeConsultaConfirmada() {
        Consulta consulta = new ConsultaRotina(paciente, clinico, data, horario, "retorno", null);
        when(consultas.findById(15L)).thenReturn(Optional.of(consulta));

        assertThrows(RegraNegocioException.class, () -> service.iniciar(15L));
        assertEquals(StatusConsulta.AGENDADA, consulta.getStatus());
    }

    @Test
    void consultaEspecializadaComProfissionalIncompativelNaoConclui() {
        // Agendada com o clinico geral por fora do service (que ja barraria), simulando dado inconsistente.
        Consulta consulta = new ConsultaEspecializada(paciente, clinico, data, horario, "dor", "obs inicial",
                Especialidade.CARDIOLOGIA);
        consulta.transitarPara(StatusConsulta.CONFIRMADA);
        consulta.iniciarAtendimento();
        when(consultas.findById(16L)).thenReturn(Optional.of(consulta));

        assertThrows(RegraNegocioException.class, () -> service.concluir(16L, "nao deve gravar"));
        assertEquals(StatusConsulta.EM_ATENDIMENTO, consulta.getStatus());
        assertEquals("obs inicial", consulta.getObservacoes());
    }

    @Test
    void consultaInexistenteLancaRecursoNaoEncontrado() {
        when(consultas.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.iniciar(99L));
        assertThrows(RecursoNaoEncontradoException.class, () -> service.concluir(99L, "x"));
        assertThrows(RecursoNaoEncontradoException.class, () -> service.naoCompareceu(99L));
    }

    private ConsultaRequest rotina() {
        return new ConsultaRequest(TipoConsulta.ROTINA, 1L, 2L, data, horario, "retorno", null, null);
    }

    private ConsultaRequest especializada(Especialidade especialidade) {
        return new ConsultaRequest(TipoConsulta.ESPECIALIZADA, 1L, 2L, data, horario, "dor no peito", null,
                especialidade);
    }
}
