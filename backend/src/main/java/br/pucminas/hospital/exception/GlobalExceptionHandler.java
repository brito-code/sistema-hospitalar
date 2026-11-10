package br.pucminas.hospital.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.JsonMappingException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unico ponto de traducao de excecao para resposta HTTP. Toda resposta de erro sai como {@link ErroResposta},
 * com a mensagem em portugues e sem expor detalhes internos (SQL, stack trace) ao cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), List.of(), req);
    }

    /** Inclui AltaInvalidaException e QuartoSemVagaException: o pedido conflita com o estado atual dos dados. */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> regraNegocio(RegraNegocioException ex, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), List.of(), req);
    }

    /** Ultima barreira do banco: CPF duplicado numa corrida entre duas requisicoes, FK, coluna maior que o limite. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> integridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violacao de integridade em {} {}: {}", req.getMethod(), req.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.CONFLICT, "Operacao viola a integridade dos dados (registro duplicado ou em uso)",
                List.of(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> problemas = new ArrayList<>(ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList());
        // erros que nao sao de um campo especifico (validacao da classe inteira)
        ex.getBindingResult().getGlobalErrors().forEach(e -> problemas.add(e.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Dados invalidos", problemas, req);
    }

    /** JSON malformado, data fora do formato, valor que nao existe no enum... */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> corpoIlegivel(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisicao invalida", campoComProblema(ex), req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tipoDeParametro(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisicao invalida", List.of(ex.getName() + ": valor invalido"), req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResposta> parametroAusente(MissingServletRequestParameterException ex,
            HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisicao invalida",
                List.of(ex.getParameterName() + ": parametro obrigatorio"), req);
    }

    /**
     * Rede de seguranca. As excecoes do proprio Spring MVC (rota inexistente, metodo HTTP nao permitido,
     * content-type nao suportado...) ja carregam o status correto, entao so o repassamos; qualquer outra coisa
     * e erro nosso: vai para o log com o stack trace e o cliente recebe uma mensagem generica.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> inesperado(Exception ex, HttpServletRequest req) {
        if (ex instanceof ErrorResponse padrao) {
            HttpStatusCode status = padrao.getStatusCode();
            return resposta(status, mensagemPadrao(status.value()), List.of(), req);
        }
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", List.of(), req);
    }

    /** Descobre qual campo do JSON causou o problema, quando o Jackson informa. */
    private static List<String> campoComProblema(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof JsonMappingException erro) {
            String campo = erro.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));
            if (!campo.isBlank()) {
                return List.of(campo + ": valor invalido");
            }
        }
        return List.of();
    }

    private static String mensagemPadrao(int status) {
        return switch (status) {
            case 404 -> "Recurso nao encontrado";
            case 405 -> "Metodo HTTP nao permitido para este recurso";
            case 406 -> "Formato de resposta nao suportado";
            case 415 -> "Tipo de conteudo nao suportado";
            default -> status < 500 ? "Requisicao invalida" : "Erro interno do servidor";
        };
    }

    private static ResponseEntity<ErroResposta> resposta(HttpStatusCode status, String mensagem,
            List<String> detalhes, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ErroResposta.de(status.value(), mensagem, req.getRequestURI(), detalhes));
    }
}
