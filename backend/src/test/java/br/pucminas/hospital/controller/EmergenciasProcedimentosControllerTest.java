package br.pucminas.hospital.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.pucminas.hospital.model.Cirurgiao;
import br.pucminas.hospital.model.ClinicoGeral;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.repository.AtendimentoEmergenciaRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProcedimentoCirurgicoRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;

@SpringBootTest
@AutoConfigureMockMvc
class EmergenciasProcedimentosControllerTest {

    /** Registros unicos: o banco de teste e compartilhado com as outras classes. */
    private static final AtomicInteger SEQUENCIA = new AtomicInteger(900000);

    @Autowired MockMvc mvc;
    @Autowired AtendimentoEmergenciaRepository emergenciaRepo;
    @Autowired ProcedimentoCirurgicoRepository procedimentoRepo;
    @Autowired PacienteRepository pacienteRepo;
    @Autowired ProfissionalSaudeRepository profissionalRepo;

    long pacienteId;
    long clinicoId;
    long cirurgiaoId;

    @BeforeEach
    void preparar() {
        limpar();
        pacienteId = pacienteRepo.save(new Paciente("Ana")).getId();
        clinicoId = profissionalRepo.save(new ClinicoGeral("Dr. X", "CRM-MG " + SEQUENCIA.incrementAndGet())).getId();
        cirurgiaoId = profissionalRepo.save(new Cirurgiao("Dr. Bisturi", "CRM-MG " + SEQUENCIA.incrementAndGet(),
                "Geral")).getId();
    }

    /** Apaga so os dados desta fatia, para nao deixar FKs que travem os testes das outras classes. */
    @AfterEach
    void limpar() {
        emergenciaRepo.deleteAll();
        procedimentoRepo.deleteAll();
    }

    private static long id(String json) {
        return Long.parseLong(json.replaceAll(".*?\"id\":(\\d+).*", "$1"));
    }

    private long registrarEmergencia(String prioridade, String chegada) throws Exception {
        String prio = prioridade == null ? "null" : "\"" + prioridade + "\"";
        String body = """
                {"pacienteId":%d,"profissionalId":%d,"prioridade":%s,"queixaPrincipal":"Dor","dataHoraChegada":"%s"}"""
                .formatted(pacienteId, clinicoId, prio, chegada);
        String resp = mvc.perform(post("/emergencias").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_AVALIACAO"))
                .andReturn().getResponse().getContentAsString();
        return id(resp);
    }

    private String corpoProcedimento(long profissionalId, String dataHora) {
        return """
                {"pacienteId":%d,"profissionalId":%d,"descricao":"Apendicectomia","tipoCirurgia":"Videolaparoscopia","dataHoraPrevista":"%s","observacoesPreOperatorias":"Jejum de 8h"}"""
                .formatted(pacienteId, profissionalId, dataHora);
    }

    @Test
    void emergenciaFluxoCompleto() throws Exception {
        long id = registrarEmergencia(null, "2026-05-10T14:00:00");

        // sem prioridade nao inicia -> 409
        mvc.perform(patch("/emergencias/" + id + "/iniciar")).andExpect(status().isConflict());

        mvc.perform(patch("/emergencias/" + id + "/classificar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"prioridade\":\"ALTA\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.prioridade").value("ALTA"));
        mvc.perform(patch("/emergencias/" + id + "/iniciar"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"));
        mvc.perform(patch("/emergencias/" + id + "/concluir").contentType(MediaType.APPLICATION_JSON)
                .content("{\"observacoes\":\"Estabilizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.observacoes").value("Estabilizado"));
        // concluir de novo -> 409
        mvc.perform(patch("/emergencias/" + id + "/concluir")).andExpect(status().isConflict());
    }

    @Test
    void filaDeEsperaOrdenadaPorPrioridade() throws Exception {
        long baixa = registrarEmergencia("BAIXA", "2026-05-10T08:00:00");
        long critica = registrarEmergencia("CRITICA", "2026-05-10T09:00:00");
        long emAtendimento = registrarEmergencia("ALTA", "2026-05-10T07:00:00");
        mvc.perform(patch("/emergencias/" + emAtendimento + "/iniciar")).andExpect(status().isOk());

        mvc.perform(get("/emergencias/aguardando")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(critica))
                .andExpect(jsonPath("$[1].id").value(baixa));
        mvc.perform(get("/emergencias").param("status", "EM_ATENDIMENTO"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void emergenciaInvalidaOuInexistente() throws Exception {
        mvc.perform(post("/emergencias").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/emergencias/9999")).andExpect(status().isNotFound());
        mvc.perform(patch("/emergencias/9999/iniciar")).andExpect(status().isNotFound());
        mvc.perform(get("/emergencias").param("status", "INVALIDO")).andExpect(status().isBadRequest());
    }

    @Test
    void procedimentoFluxoCompleto() throws Exception {
        String resp = mvc.perform(post("/procedimentos").contentType(MediaType.APPLICATION_JSON)
                .content(corpoProcedimento(cirurgiaoId, "2026-06-01T09:00:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADO"))
                .andExpect(jsonPath("$.tipoCirurgia").value("Videolaparoscopia"))
                .andExpect(jsonPath("$.dataHoraPrevista").value("2026-06-01T09:00:00"))
                .andExpect(jsonPath("$.observacoesPreOperatorias").value("Jejum de 8h"))
                .andReturn().getResponse().getContentAsString();
        long id = id(resp);

        // concluir sem iniciar -> 409
        mvc.perform(patch("/procedimentos/" + id + "/concluir")).andExpect(status().isConflict());

        mvc.perform(patch("/procedimentos/" + id + "/iniciar"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
        mvc.perform(patch("/procedimentos/" + id + "/concluir").contentType(MediaType.APPLICATION_JSON)
                .content("{\"observacoesPosOperatorias\":\"Sem intercorrencias\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.observacoesPosOperatorias").value("Sem intercorrencias"))
                .andExpect(jsonPath("$.observacoesPreOperatorias").value("Jejum de 8h"))
                .andExpect(jsonPath("$.dataHoraInicio").isNotEmpty())
                .andExpect(jsonPath("$.dataHoraFim").isNotEmpty());

        mvc.perform(get("/procedimentos").param("profissionalId", String.valueOf(cirurgiaoId)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void procedimentoSemTipoDeCirurgiaRetorna400() throws Exception {
        String corpo = """
                {"pacienteId":%d,"profissionalId":%d,"descricao":"Apendicectomia","dataHoraPrevista":"2026-06-03T09:00:00"}"""
                .formatted(pacienteId, cirurgiaoId);
        mvc.perform(post("/procedimentos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void procedimentoComResponsavelQueNaoECirurgiaoRetorna409() throws Exception {
        mvc.perform(post("/procedimentos").contentType(MediaType.APPLICATION_JSON)
                .content(corpoProcedimento(clinicoId, "2026-06-01T10:00:00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Responsavel pelo procedimento cirurgico precisa ser um cirurgiao"));
    }

    @Test
    void cirurgiaoNaoTemDoisProcedimentosNoMesmoHorario() throws Exception {
        String corpo = corpoProcedimento(cirurgiaoId, "2026-06-02T09:00:00");
        String resp = mvc.perform(post("/procedimentos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        mvc.perform(post("/procedimentos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict());

        // depois de cancelado, o horario fica livre
        mvc.perform(patch("/procedimentos/" + id(resp) + "/cancelar"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADO"));
        mvc.perform(post("/procedimentos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());
    }

    @Test
    void procedimentoInvalidoOuInexistente() throws Exception {
        mvc.perform(post("/procedimentos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/procedimentos/9999")).andExpect(status().isNotFound());
        mvc.perform(post("/procedimentos").contentType(MediaType.APPLICATION_JSON)
                .content(corpoProcedimento(9999, "2026-06-01T09:00:00")))
                .andExpect(status().isNotFound());
    }
}
