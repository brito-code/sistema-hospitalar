package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.enums.Especialidade;
import jakarta.validation.constraints.NotNull;

public record EncaminhamentoRequest(@NotNull Long pacienteId, @NotNull Especialidade especialidade) {
}
