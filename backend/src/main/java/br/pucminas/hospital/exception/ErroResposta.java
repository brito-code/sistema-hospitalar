package br.pucminas.hospital.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Corpo padrao de toda resposta de erro da API. {@code detalhes} traz os problemas campo a campo
 * (no formato "campo: mensagem") quando ha mais de um, e vem vazio nos demais casos.
 */
public record ErroResposta(LocalDateTime timestamp, int status, String mensagem, String caminho,
        List<String> detalhes) {

    public static ErroResposta de(int status, String mensagem, String caminho, List<String> detalhes) {
        return new ErroResposta(LocalDateTime.now(), status, mensagem, caminho, detalhes);
    }
}
