package br.pucminas.hospital.dto;

import java.time.LocalDateTime;

import br.pucminas.hospital.model.enums.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Chegada do paciente. Prioridade e opcional (pode vir depois, na triagem); sem data, usa o momento atual. */
public record EmergenciaRequest(
        @NotNull Long pacienteId,
        @NotNull Long profissionalId,
        Prioridade prioridade,
        @NotBlank String queixaPrincipal,
        LocalDateTime dataHoraChegada) {
}
