package br.pucminas.hospital.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.ErrorResponseException;

/** Testa o handler direto, sem subir o Spring: cobre os caminhos que e dificil provocar por HTTP. */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest requisicao = new MockHttpServletRequest("POST", "/pacientes");

    @Test
    void regraDeNegocioViraConflito() {
        ResponseEntity<ErroResposta> resposta = handler
                .regraNegocio(new RegraNegocioException("Quarto lotado"), requisicao);

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertEquals(409, resposta.getBody().status());
        assertEquals("Quarto lotado", resposta.getBody().mensagem());
        assertEquals("/pacientes", resposta.getBody().caminho());
        assertTrue(resposta.getBody().detalhes().isEmpty());
        assertNotNull(resposta.getBody().timestamp());
    }

    @Test
    void subclassesDeRegraDeNegocioTambemSaoConflito() {
        ResponseEntity<ErroResposta> resposta = handler
                .regraNegocio(new QuartoSemVagaException(7L), requisicao);

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
    }

    @Test
    void recursoInexistenteViraNaoEncontrado() {
        ResponseEntity<ErroResposta> resposta = handler
                .naoEncontrado(new RecursoNaoEncontradoException("Paciente", 3L), requisicao);

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertEquals("Paciente nao encontrado(a): id 3", resposta.getBody().mensagem());
    }

    @Test
    void violacaoDeIntegridadeNaoExpoeOSqlParaOCliente() {
        DataIntegrityViolationException erro = new DataIntegrityViolationException(
                "could not execute statement [Unique index or primary key violation: CPF]");

        ResponseEntity<ErroResposta> resposta = handler.integridade(erro, requisicao);

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertFalse(resposta.getBody().mensagem().contains("statement"));
        assertFalse(resposta.getBody().mensagem().contains("CPF"));
    }

    @Test
    void excecaoDoSpringMvcMantemOStatusQueElaJaCarrega() {
        ResponseEntity<ErroResposta> resposta = handler
                .inesperado(new ErrorResponseException(HttpStatus.NOT_FOUND), requisicao);

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertEquals("Recurso nao encontrado", resposta.getBody().mensagem());
    }

    @Test
    void erroInesperadoViraErro500SemVazarAMensagemOriginal() {
        ResponseEntity<ErroResposta> resposta = handler
                .inesperado(new IllegalStateException("senha do banco: 1234"), requisicao);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.getStatusCode());
        assertEquals(500, resposta.getBody().status());
        assertEquals("Erro interno do servidor", resposta.getBody().mensagem());
        assertFalse(resposta.getBody().mensagem().contains("senha"));
    }
}
