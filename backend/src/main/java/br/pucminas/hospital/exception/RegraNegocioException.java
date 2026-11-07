package br.pucminas.hospital.exception;

/** Violacao de regra de negocio (mapeada para HTTP 409). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
