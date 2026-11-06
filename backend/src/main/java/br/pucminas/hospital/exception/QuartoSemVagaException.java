package br.pucminas.hospital.exception;

/** RN4/RN5: quarto sem vaga disponivel. */
public class QuartoSemVagaException extends RegraNegocioException {

    public QuartoSemVagaException(Long quartoId) {
        super("Quarto " + quartoId + " nao possui vaga disponivel");
    }
}
