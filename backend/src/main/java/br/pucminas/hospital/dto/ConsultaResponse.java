package br.pucminas.hospital.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import br.pucminas.hospital.model.Consulta;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.model.enums.StatusConsulta;
import br.pucminas.hospital.model.enums.TipoConsulta;

public record ConsultaResponse(Long id, TipoConsulta tipo, Long pacienteId, Long profissionalId, LocalDate data,
        LocalTime horario, StatusConsulta status, String motivo, String observacoesMedicas,
        Especialidade especialidade) {

    public static ConsultaResponse de(Consulta consulta) {
        return new ConsultaResponse(consulta.getId(), consulta.getTipo(), consulta.getPaciente().getId(),
                consulta.getProfissionalResponsavel().getId(), consulta.getData(), consulta.getHorario(),
                consulta.getStatus(), consulta.getMotivo(), consulta.getObservacoes(),
                consulta.getEspecialidadeSolicitada());
    }
}
