package br.pucminas.hospital.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record ReagendarRequest(@NotNull LocalDate novaData, @NotNull LocalTime novoHorario) {
}
