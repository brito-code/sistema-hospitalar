package br.pucminas.hospital.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.model.enums.TipoConsulta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConsultaRequest(
        @NotNull TipoConsulta tipo,
        @NotNull Long pacienteId,
        @NotNull Long profissionalId,
        @NotNull LocalDate data,
        @NotNull LocalTime horario,
        @NotBlank String motivo,
        String observacoesMedicas,
        Especialidade especialidade) {
}
