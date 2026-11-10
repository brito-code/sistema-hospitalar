package br.pucminas.hospital.dto;

import java.time.LocalDateTime;

import br.pucminas.hospital.model.AtendimentoEmergencia;
import br.pucminas.hospital.model.enums.Prioridade;
import br.pucminas.hospital.model.enums.StatusEmergencia;

public record EmergenciaResponse(Long id, Long pacienteId, Long profissionalId, Prioridade prioridade,
        StatusEmergencia status, String queixaPrincipal, LocalDateTime dataHoraChegada,
        LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, String observacoes) {

    public static EmergenciaResponse de(AtendimentoEmergencia e) {
        return new EmergenciaResponse(e.getId(), e.getPaciente().getId(), e.getProfissionalResponsavel().getId(),
                e.getPrioridade(), e.getStatus(), e.getQueixaPrincipal(), e.getDataHoraChegada(),
                e.getDataHoraInicio(), e.getDataHoraFim(), e.getObservacoes());
    }
}
