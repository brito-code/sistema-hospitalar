package br.pucminas.hospital.dto;

/** Corpo opcional do PATCH de conclusao (emergencia ou procedimento). */
public record ConclusaoRequest(String observacoes) {
}
