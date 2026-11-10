package br.pucminas.hospital.model;

import br.pucminas.hospital.model.enums.Especialidade;

/** Resultado de {@link ClinicoGeral#encaminhar(Paciente, Especialidade)}. Nao e persistido. */
public record Encaminhamento(ClinicoGeral clinicoGeral, Paciente paciente, Especialidade especialidade) {
}
