package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.enums.TipoQuarto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuartoRequest(
        @NotBlank String numero,
        @NotNull Integer andar,
        @NotNull @Min(1) Integer capacidadeMaxima,
        @NotNull TipoQuarto tipo) {
}
