package br.pucminas.hospital.model;

import java.time.LocalDateTime;

import br.pucminas.hospital.model.enums.Prioridade;
import br.pucminas.hospital.model.enums.StatusEmergencia;

// TODO(dono: Emergencia): mapeamento JPA e implementacao.
public class AtendimentoEmergencia extends Atendimento {

    private Prioridade prioridade;
    private StatusEmergencia status;

    @Override
    protected void validarRealizacao() {
        throw new UnsupportedOperationException("TODO(dono: Emergencia)");
    }

    @Override
    public LocalDateTime getDataReferencia() {
        throw new UnsupportedOperationException("TODO(dono: Emergencia)");
    }
}
