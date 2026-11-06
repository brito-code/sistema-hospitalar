package br.pucminas.hospital.model;

import java.time.LocalDateTime;

import br.pucminas.hospital.model.enums.Especialidade;

// TODO(dono: Consulta): implementar.
public class ConsultaEspecializada extends Consulta {

    private Especialidade especialidade;

    @Override
    protected void validarRealizacao() {
        throw new UnsupportedOperationException("TODO(dono: Consulta)");
    }

    @Override
    public LocalDateTime getDataReferencia() {
        throw new UnsupportedOperationException("TODO(dono: Consulta)");
    }
}
