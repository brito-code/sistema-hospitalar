package br.pucminas.hospital.dto;

import java.time.LocalDate;

import br.pucminas.hospital.model.Internacao;

public record InternacaoResponse(Long id, Long pacienteId, Long profissionalId, Long quartoId,
        LocalDate dataEntrada, LocalDate dataPrevistaAlta, LocalDate dataEfetivaAlta,
        String observacoes, boolean ativa, long diasInternado) {

    public static InternacaoResponse de(Internacao i) {
        return new InternacaoResponse(i.getId(), i.getPaciente().getId(), i.getProfissionalResponsavel().getId(),
                i.getQuarto().getId(), i.getDataEntrada(), i.getDataPrevistaAlta(), i.getDataEfetivaAlta(),
                i.getObservacoes(), i.isAtiva(), i.getDiasInternado());
    }
}
