package br.pucminas.hospital.model;

import java.time.LocalDateTime;

// TODO(dono: Consulta): implementar.
public class ConsultaRotina extends Consulta {

    @Override
    protected void validarRealizacao() {
        throw new UnsupportedOperationException("TODO(dono: Consulta)");
    }

    @Override
    public LocalDateTime getDataReferencia() {
        throw new UnsupportedOperationException("TODO(dono: Consulta)");
    }
}
