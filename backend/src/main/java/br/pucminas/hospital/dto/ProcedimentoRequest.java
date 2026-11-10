package br.pucminas.hospital.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProcedimentoRequest(
        @NotNull Long pacienteId,
        @NotNull Long profissionalId,
        @NotBlank String descricao,
        @NotBlank String tipoCirurgia,
        @NotNull LocalDateTime dataHoraPrevista,
        String observacoesPreOperatorias) {
}
