package br.pucminas.hospital.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.pucminas.hospital.model.ClinicoGeral;
import br.pucminas.hospital.model.Consulta;
import br.pucminas.hospital.model.ConsultaEspecializada;
import br.pucminas.hospital.model.MedicoEspecialista;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.model.enums.StatusConsulta;
import br.pucminas.hospital.repository.ConsultaRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ConsultaControllerTest {

    /** Registros unicos: o banco de teste e compartilhado com as outras classes. */
    private static final AtomicInteger SEQUENCIA = new AtomicInteger(800000);

    @Autowired MockMvc mvc;
    @Autowired PacienteRepository pacienteRepo;
    @Autowired ProfissionalSaudeRepository profissionalRepo;
    @Autowired ConsultaRepository consultaRepo;

    final List<Long> criadas = new ArrayList<>();
    long pacienteId;
    long clinicoId;
    long cardiologistaId;
    int dia;

    @BeforeEach
    void preparar() {
        pacienteId = pacienteRepo.save(new Paciente("Ana")).getId();
        clinicoId = profissionalRepo.save(new ClinicoGeral("Dr. Geral", novoCrm())).getId();
        cardiologistaId = profissionalRepo
                .save(new MedicoEspecialista("Dra. Cora", novoCrm(), Especialidade.CARDIOLOGIA)).getId();
        dia = SEQUENCIA.incrementAndGet() % 300;
    }

    /** Remove so as consultas criadas aqui, sem tocar nos dados das outras classes. */
    @AfterEach
    void limpar() {
        consultaRepo.deleteAllById(criadas);
        criadas.clear();
    }

    private static String novoCrm() {
        return "CRM-MG " + SEQUENCIA.incrementAndGet();
    }

    private static long id(String json) {
        return Long.parseLong(json.replaceAll(".*?\"id\":(\\d+).*", "$1"));
    }

    private long agendarRotina() throws Exception {
        String body = """
                {"tipo":"ROTINA","pacienteId":%d,"profissionalId":%d,"data":"%s","horario":"09:00","motivo":"retorno"}"""
                .formatted(pacienteId, clinicoId, LocalDate.of(2030, 1, 1).plusDays(dia));
        String resp = mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andReturn().getResponse().getContentAsString();
        long id = id(resp);
        criadas.add(id);
        return id;
    }

    private void transicionar(long id, String acao, String esperado) throws Exception {
        mvc.perform(patch("/consultas/" + id + "/" + acao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(esperado));
    }

    @Test
    void fluxoCompletoAteConcluirComObservacoes() throws Exception {
        long id = agendarRotina();

        transicionar(id, "confirmar", "CONFIRMADA");
        transicionar(id, "iniciar", "EM_ATENDIMENTO");
        mvc.perform(patch("/consultas/" + id + "/concluir").contentType(MediaType.APPLICATION_JSON)
                .content("{\"observacoes\":\"Paciente estavel\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.observacoesMedicas").value("Paciente estavel"));

        mvc.perform(get("/consultas/" + id))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.observacoesMedicas").value("Paciente estavel"));
    }

    @Test
    void concluirSemCorpoEPermitido() throws Exception {
        long id = agendarRotina();
        transicionar(id, "confirmar", "CONFIRMADA");
        transicionar(id, "iniciar", "EM_ATENDIMENTO");

        transicionar(id, "concluir", "CONCLUIDA");
    }

    @Test
    void concluirSemIniciarDevolve409() throws Exception {
        long id = agendarRotina();
        transicionar(id, "confirmar", "CONFIRMADA");

        mvc.perform(patch("/consultas/" + id + "/concluir")).andExpect(status().isConflict());
        mvc.perform(get("/consultas/" + id)).andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }

    @Test
    void iniciarSemConfirmarDevolve409() throws Exception {
        long id = agendarRotina();

        mvc.perform(patch("/consultas/" + id + "/iniciar")).andExpect(status().isConflict());
    }

    @Test
    void consultaCanceladaNaoPodeSerConcluida() throws Exception {
        long id = agendarRotina();
        transicionar(id, "cancelar", "CANCELADA");

        mvc.perform(patch("/consultas/" + id + "/concluir")).andExpect(status().isConflict());
        mvc.perform(patch("/consultas/" + id + "/iniciar")).andExpect(status().isConflict());
    }

    @Test
    void consultaConcluidaNaoVolta() throws Exception {
        long id = agendarRotina();
        transicionar(id, "confirmar", "CONFIRMADA");
        transicionar(id, "iniciar", "EM_ATENDIMENTO");
        transicionar(id, "concluir", "CONCLUIDA");

        mvc.perform(patch("/consultas/" + id + "/confirmar")).andExpect(status().isConflict());
        mvc.perform(patch("/consultas/" + id + "/cancelar")).andExpect(status().isConflict());
        mvc.perform(patch("/consultas/" + id + "/nao-compareceu")).andExpect(status().isConflict());
    }

    @Test
    void naoComparecimentoAPartirDeAgendadaEBloqueadoDepois() throws Exception {
        long id = agendarRotina();
        transicionar(id, "nao-compareceu", "NAO_COMPARECEU");

        mvc.perform(patch("/consultas/" + id + "/confirmar")).andExpect(status().isConflict());
        mvc.perform(patch("/consultas/" + id + "/nao-compareceu")).andExpect(status().isConflict());
    }

    @Test
    void naoComparecimentoAPartirDeConfirmada() throws Exception {
        long id = agendarRotina();
        transicionar(id, "confirmar", "CONFIRMADA");

        transicionar(id, "nao-compareceu", "NAO_COMPARECEU");
    }

    @Test
    void naoComparecimentoEmAtendimentoDevolve409() throws Exception {
        long id = agendarRotina();
        transicionar(id, "confirmar", "CONFIRMADA");
        transicionar(id, "iniciar", "EM_ATENDIMENTO");

        mvc.perform(patch("/consultas/" + id + "/nao-compareceu")).andExpect(status().isConflict());
    }

    @Test
    void especializadaComProfissionalIncompativelNaoConclui() throws Exception {
        // Gravada direto no repositorio: o agendamento pela API ja barra esse caso.
        ProfissionalSaude clinico = profissionalRepo.findById(clinicoId).orElseThrow();
        Paciente paciente = pacienteRepo.findById(pacienteId).orElseThrow();
        Consulta consulta = new ConsultaEspecializada(paciente, clinico, LocalDate.of(2031, 1, 1).plusDays(dia),
                LocalTime.of(10, 0), "dor no peito", "obs inicial", Especialidade.CARDIOLOGIA);
        consulta.transitarPara(StatusConsulta.CONFIRMADA);
        consulta.iniciarAtendimento();
        long id = consultaRepo.save(consulta).getId();
        criadas.add(id);

        mvc.perform(patch("/consultas/" + id + "/concluir").contentType(MediaType.APPLICATION_JSON)
                .content("{\"observacoes\":\"nao deve gravar\"}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/consultas/" + id))
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"))
                .andExpect(jsonPath("$.observacoesMedicas").value("obs inicial"));
    }

    @Test
    void especializadaComEspecialistaCompativelConclui() throws Exception {
        String body = """
                {"tipo":"ESPECIALIZADA","pacienteId":%d,"profissionalId":%d,"data":"%s","horario":"11:00",
                 "motivo":"dor no peito","especialidade":"CARDIOLOGIA"}"""
                .formatted(pacienteId, cardiologistaId, LocalDate.of(2032, 1, 1).plusDays(dia));
        String resp = mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = id(resp);
        criadas.add(id);

        transicionar(id, "confirmar", "CONFIRMADA");
        transicionar(id, "iniciar", "EM_ATENDIMENTO");
        transicionar(id, "concluir", "CONCLUIDA");
    }

    @Test
    void consultaInexistenteDevolve404() throws Exception {
        mvc.perform(patch("/consultas/999999/iniciar")).andExpect(status().isNotFound());
        mvc.perform(patch("/consultas/999999/concluir")).andExpect(status().isNotFound());
        mvc.perform(patch("/consultas/999999/nao-compareceu")).andExpect(status().isNotFound());
    }
}
