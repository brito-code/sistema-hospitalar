package br.pucminas.hospital.dto;

import java.util.List;

import br.pucminas.hospital.model.Encaminhamento;
import br.pucminas.hospital.model.MedicoEspecialista;
import br.pucminas.hospital.model.enums.Especialidade;

/** Encaminhamento feito pelo clinico geral, com os especialistas que podem receber o paciente. */
public record EncaminhamentoResponse(Long clinicoGeralId, Long pacienteId, Especialidade especialidade,
        List<ProfissionalResponse> especialistasDisponiveis) {

    public static EncaminhamentoResponse de(Encaminhamento encaminhamento, List<MedicoEspecialista> especialistas) {
        return new EncaminhamentoResponse(encaminhamento.clinicoGeral().getId(), encaminhamento.paciente().getId(),
                encaminhamento.especialidade(), especialistas.stream().map(ProfissionalResponse::de).toList());
    }
}
