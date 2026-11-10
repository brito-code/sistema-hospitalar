package br.pucminas.hospital.dto;

import java.time.LocalDateTime;

import br.pucminas.hospital.model.ProcedimentoCirurgico;
import br.pucminas.hospital.model.enums.StatusProcedimento;

public record ProcedimentoResponse(Long id, Long pacienteId, Long profissionalId, String descricao,
        String tipoCirurgia, LocalDateTime dataHoraPrevista, StatusProcedimento status,
        LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, String observacoesPreOperatorias,
        String observacoesPosOperatorias, String observacoes) {

    public static ProcedimentoResponse de(ProcedimentoCirurgico p) {
        return new ProcedimentoResponse(p.getId(), p.getPaciente().getId(), p.getProfissionalResponsavel().getId(),
                p.getDescricao(), p.getTipoCirurgia(), p.getDataHoraPrevista(), p.getStatus(),
                p.getDataHoraInicio(), p.getDataHoraFim(), p.getObservacoesPreOperatorias(),
                p.getObservacoesPosOperatorias(), p.getObservacoes());
    }
}
