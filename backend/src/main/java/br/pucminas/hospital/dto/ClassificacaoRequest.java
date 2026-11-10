package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.enums.Prioridade;
import jakarta.validation.constraints.NotNull;

/** Corpo do PATCH de triagem de uma emergencia. */
public record ClassificacaoRequest(@NotNull Prioridade prioridade) {
}
