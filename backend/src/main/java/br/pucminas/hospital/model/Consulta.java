package br.pucminas.hospital.model;

import br.pucminas.hospital.model.enums.StatusConsulta;

// TODO(dono: Consulta): mapeamento JPA, data/horario, motivo e regras. Stub apenas com heranca.
public abstract class Consulta extends Atendimento {

    private StatusConsulta status;

    protected Consulta() {
    }
}
