package br.pucminas.hospital.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Todo erro da API sai no mesmo formato (ErroResposta), inclusive os que o Spring MVC gera sozinho. */
@SpringBootTest
@AutoConfigureMockMvc
class RespostasDeErroTest {

    @Autowired MockMvc mvc;

    @Test
    void rotaInexistenteRetorna404NoFormatoPadrao() throws Exception {
        mvc.perform(get("/rota-que-nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Recurso nao encontrado"))
                .andExpect(jsonPath("$.caminho").value("/rota-que-nao-existe"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.detalhes").isEmpty());
    }

    @Test
    void metodoNaoPermitidoRetorna405() throws Exception {
        mvc.perform(put("/pacientes").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.mensagem").value("Metodo HTTP nao permitido para este recurso"));
    }

    @Test
    void contentTypeNaoSuportadoRetorna415() throws Exception {
        mvc.perform(post("/pacientes").contentType(MediaType.TEXT_PLAIN).content("qualquer coisa"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.mensagem").value("Tipo de conteudo nao suportado"));
    }

    @Test
    void jsonMalformadoRetorna400() throws Exception {
        mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Requisicao invalida"))
                .andExpect(jsonPath("$.caminho").value("/pacientes"));
    }

    @Test
    void corpoAusenteRetorna400() throws Exception {
        mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void idQueNaoEhNumeroRetorna400ApontandoOParametro() throws Exception {
        mvc.perform(get("/pacientes/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Requisicao invalida"))
                .andExpect(jsonPath("$.detalhes[0]").value("id: valor invalido"));
    }

    @Test
    void erroDeOutrasFatiasUsaOMesmoFormato() throws Exception {
        mvc.perform(get("/quartos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.caminho").value("/quartos/999999"));
    }
}
