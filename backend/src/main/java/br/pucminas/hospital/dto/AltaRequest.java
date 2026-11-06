package br.pucminas.hospital.dto;

import java.time.LocalDate;

/** Corpo opcional do PATCH de alta. Sem data, usa a data de hoje. */
public record AltaRequest(LocalDate dataEfetivaAlta) {
}
