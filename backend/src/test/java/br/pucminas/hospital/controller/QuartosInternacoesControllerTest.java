package br.pucminas.hospital.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.repository.InternacaoRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;
import br.pucminas.hospital.repository.QuartoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class QuartosInternacoesControllerTest {

    @Autowired MockMvc mvc;
    @Autowired InternacaoRepository internacaoRepo;
    @Autowired QuartoRepository quartoRepo;
    @Autowired PacienteRepository pacienteRepo;
    @Autowired ProfissionalSaudeRepository profissionalRepo;

    long pacienteId;
    long profissionalId;

    @BeforeEach
    void preparar() {
        internacaoRepo.deleteAll();
        quartoRepo.deleteAll();
        pacienteId = pacienteRepo.save(new Paciente("Ana")).getId();
        profissionalId = profissionalRepo.save(new ProfissionalSaude("Dr. X")).getId();
    }

    private long criarQuarto(int capacidade) throws Exception {
        String body = """
                {"numero":"201","andar":2,"capacidadeMaxima":%d,"tipo":"ENFERMARIA"}""".formatted(capacidade);
        String resp = mvc.perform(post("/quartos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.situacao").value("DISPONIVEL"))
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(resp.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private String corpoInternacao(long quartoId) {
        return """
                {"pacienteId":%d,"profissionalId":%d,"quartoId":%d,"dataEntrada":"2026-05-10"}"""
                .formatted(pacienteId, profissionalId, quartoId);
    }

    @Test
    void quartosCrudBasico() throws Exception {
        long id = criarQuarto(2);
        mvc.perform(get("/quartos/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.numero").value("201"));
        mvc.perform(get("/quartos").param("situacao", "DISPONIVEL")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(put("/quartos/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"numero\":\"202\",\"andar\":3,\"capacidadeMaxima\":4,\"tipo\":\"UTI\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tipo").value("UTI"));
        mvc.perform(get("/quartos/9999")).andExpect(status().isNotFound());
    }

    @Test
    void quartoInvalidoRetorna400() throws Exception {
        mvc.perform(post("/quartos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"numero\":\"\",\"andar\":1,\"capacidadeMaxima\":0,\"tipo\":\"UTI\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/quartos").param("situacao", "INVALIDA")).andExpect(status().isBadRequest());
    }

    @Test
    void internacaoFluxoCompleto() throws Exception {
        long quarto = criarQuarto(1);
        String resp = mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON).content(corpoInternacao(quarto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativa").value(true))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(resp.replaceAll(".*\"id\":(\\d+).*", "$1"));

        // quarto lotado -> 409
        mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON).content(corpoInternacao(quarto)))
                .andExpect(status().isConflict());
        mvc.perform(get("/quartos/" + quarto)).andExpect(jsonPath("$.situacao").value("OCUPADO"));

        mvc.perform(get("/internacoes")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/internacoes/" + id)).andExpect(status().isOk());

        // alta antes da entrada -> 409
        mvc.perform(patch("/internacoes/" + id + "/alta").contentType(MediaType.APPLICATION_JSON)
                .content("{\"dataEfetivaAlta\":\"2026-05-01\"}")).andExpect(status().isConflict());
        mvc.perform(patch("/internacoes/" + id + "/alta").contentType(MediaType.APPLICATION_JSON)
                .content("{\"dataEfetivaAlta\":\"2026-05-12\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativa").value(false));
        // alta duplicada -> 409
        mvc.perform(patch("/internacoes/" + id + "/alta")).andExpect(status().isConflict());
        mvc.perform(get("/quartos/" + quarto)).andExpect(jsonPath("$.situacao").value("DISPONIVEL"));
    }

    @Test
    void internacaoInvalidaOuInexistente() throws Exception {
        mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON).content(corpoInternacao(9999)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/internacoes/9999")).andExpect(status().isNotFound());
        mvc.perform(patch("/internacoes/9999/alta")).andExpect(status().isNotFound());
    }
}
