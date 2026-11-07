package br.pucminas.hospital.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record InternacaoRequest(
        @NotNull Long pacienteId,
        @NotNull Long profissionalId,
        @NotNull Long quartoId,
        @NotNull LocalDate dataEntrada,
        LocalDate dataPrevistaAlta,
        String observacoes) {
}
