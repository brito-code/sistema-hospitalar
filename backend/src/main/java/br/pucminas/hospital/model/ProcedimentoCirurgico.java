package br.pucminas.hospital.model;

import java.time.LocalDateTime;

import br.pucminas.hospital.model.enums.StatusProcedimento;

// TODO(dono: Procedimento): mapeamento JPA e implementacao.
public class ProcedimentoCirurgico extends Atendimento {

    private StatusProcedimento status;

    @Override
    protected void validarRealizacao() {
        throw new UnsupportedOperationException("TODO(dono: Procedimento)");
    }

    @Override
    public LocalDateTime getDataReferencia() {
        throw new UnsupportedOperationException("TODO(dono: Procedimento)");
    }
}
