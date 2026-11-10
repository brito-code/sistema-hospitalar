package br.pucminas.hospital.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.pucminas.hospital.dto.ProcedimentoRequest;
import br.pucminas.hospital.dto.ProcedimentoResponse;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.Cirurgiao;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProcedimentoCirurgico;
import br.pucminas.hospital.model.enums.StatusProcedimento;
import br.pucminas.hospital.repository.ConsultaRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProcedimentoCirurgicoRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;

@ExtendWith(MockitoExtension.class)
class ProcedimentoServiceTest {

    @Mock ProcedimentoCirurgicoRepository procedimentos;
    @Mock ConsultaRepository consultas;
    @Mock PacienteRepository pacientes;
    @Mock ProfissionalSaudeRepository profissionais;
    @InjectMocks ProcedimentoService service;

    final LocalDateTime quando = LocalDateTime.of(2026, 6, 1, 9, 0);

    @BeforeEach
    void preparar() {
        Paciente paciente = new Paciente("Ana");
        ReflectionTestUtils.setField(paciente, "id", 1L);
        Cirurgiao cirurgiao = new Cirurgiao("Dr. Bisturi", "CRM-MG 20001", "Geral");
        ReflectionTestUtils.setField(cirurgiao, "id", 2L);
        lenient().when(pacientes.findById(1L)).thenReturn(Optional.of(paciente));
        lenient().when(profissionais.findById(2L)).thenReturn(Optional.of(cirurgiao));
        lenient().when(procedimentos.save(any(ProcedimentoCirurgico.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private ProcedimentoRequest pedido() {
        return new ProcedimentoRequest(1L, 2L, "Apendicectomia", quando);
    }

    @Test
    void agendaQuandoHorarioLivre() {
        ProcedimentoResponse criado = service.agendar(pedido());
        assertEquals(StatusProcedimento.AGENDADO, criado.status());
        verify(procedimentos).save(any(ProcedimentoCirurgico.class));
    }

    @Test
    void bloqueiaOutroProcedimentoNoMesmoHorario() {
        when(procedimentos.existeConflito(2L, quando)).thenReturn(true);
        assertThrows(RegraNegocioException.class, () -> service.agendar(pedido()));
        verify(procedimentos, never()).save(any());
    }

    @Test
    void bloqueiaConsultaNoMesmoHorario() {
        when(consultas.existeConflito(2L, quando.toLocalDate(), quando.toLocalTime())).thenReturn(true);
        assertThrows(RegraNegocioException.class, () -> service.agendar(pedido()));
        verify(procedimentos, never()).save(any());
    }
}
