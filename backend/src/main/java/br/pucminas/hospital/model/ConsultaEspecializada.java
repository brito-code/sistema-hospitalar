package br.pucminas.hospital.model;

import java.time.LocalDate;
import java.time.LocalTime;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.model.enums.TipoConsulta;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** Consulta ligada a uma especialidade, que o profissional responsavel precisa atender. */
@Entity
public class ConsultaEspecializada extends Consulta {

    @Enumerated(EnumType.STRING)
    private Especialidade especialidade;

    protected ConsultaEspecializada() {
    }

    public ConsultaEspecializada(Paciente paciente, ProfissionalSaude profissional, LocalDate data, LocalTime horario,
            String motivo, String observacoesMedicas, Especialidade especialidade) {
        super(paciente, profissional, data, horario, motivo, observacoesMedicas);
        this.especialidade = especialidade;
    }

    @Override
    protected void validarRealizacao() {
        if (especialidade == null) {
            throw new RegraNegocioException("Consulta especializada exige especialidade");
        }
        ProfissionalSaude profissional = getProfissionalResponsavel();
        if (profissional == null || !profissional.atendeEspecialidade(especialidade)) {
            throw new RegraNegocioException("Profissional nao atende a especialidade " + especialidade);
        }
    }

    @Override
    public TipoConsulta getTipo() {
        return TipoConsulta.ESPECIALIZADA;
    }

    @Override
    public Especialidade getEspecialidadeSolicitada() {
        return especialidade;
    }

    @Override
    public Consulta reagendar(LocalDate novaData, LocalTime novoHorario) {
        return new ConsultaEspecializada(getPaciente(), getProfissionalResponsavel(), novaData, novoHorario,
                getMotivo(), getObservacoes(), especialidade);
    }
}
