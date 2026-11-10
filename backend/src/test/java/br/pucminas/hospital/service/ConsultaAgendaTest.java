package br.pucminas.hospital.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.pucminas.hospital.dto.ConsultaRequest;
import br.pucminas.hospital.dto.ConsultaResponse;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.MedicoEspecialista;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.model.enums.StatusConsulta;
import br.pucminas.hospital.model.enums.TipoConsulta;
import br.pucminas.hospital.repository.ConsultaRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;

@SpringBootTest
class ConsultaAgendaTest {

    @Autowired ConsultaService service;
    @Autowired ConsultaRepository consultas;
    @Autowired PacienteRepository pacientes;
    @Autowired ProfissionalSaudeRepository profissionais;

    Long pacienteId;
    Long profissionalId;
    final LocalDate data = LocalDate.of(2026, 10, 6);
    final LocalTime horario = LocalTime.of(8, 0);

    @BeforeEach
    void preparar() {
        consultas.deleteAll();
        pacienteId = pacientes.save(new Paciente("Ana")).getId();
        MedicoEspecialista profissional = new MedicoEspecialista("Dra. Mariana", "CRM-MG 45231", Especialidade.CARDIOLOGIA);
        profissionalId = profissionais.save(profissional).getId();
    }

    @Test
    void consultaCanceladaLiberaOHorarioEOutraAgendadaBloqueia() {
        ConsultaResponse primeira = service.agendar(rotina());
        service.cancelar(primeira.id());
        ConsultaResponse segunda = service.agendar(rotina());

        assertEquals(StatusConsulta.CANCELADA, service.buscar(primeira.id()).status());
        assertEquals(StatusConsulta.AGENDADA, segunda.status());
        assertThrows(RegraNegocioException.class, () -> service.agendar(rotina()));
    }

    @Test
    void especialidadeIncompativelNaoGravaAConsulta() {
        assertThrows(RegraNegocioException.class, () -> service.agendar(especializada(Especialidade.ORTOPEDIA)));
        assertEquals(0, consultas.count());

        ConsultaResponse criada = service.agendar(especializada(Especialidade.CARDIOLOGIA));
        assertEquals(Especialidade.CARDIOLOGIA, criada.especialidade());
        assertEquals(1, consultas.count());
    }

    private ConsultaRequest rotina() {
        return new ConsultaRequest(TipoConsulta.ROTINA, pacienteId, profissionalId, data, horario, "retorno", null,
                null);
    }

    private ConsultaRequest especializada(Especialidade especialidade) {
        return new ConsultaRequest(TipoConsulta.ESPECIALIZADA, pacienteId, profissionalId, data, horario,
                "dor no peito", null, especialidade);
    }
}
