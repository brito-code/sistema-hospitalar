package br.pucminas.hospital.model;

import java.time.LocalDate;
import java.time.LocalTime;

import br.pucminas.hospital.model.enums.TipoConsulta;
import jakarta.persistence.Entity;

/** Consulta sem exigencia de especialidade medica. */
@Entity
public class ConsultaRotina extends Consulta {

    protected ConsultaRotina() {
    }

    public ConsultaRotina(Paciente paciente, ProfissionalSaude profissional, LocalDate data, LocalTime horario,
            String motivo, String observacoesMedicas) {
        super(paciente, profissional, data, horario, motivo, observacoesMedicas);
    }

    @Override
    protected void validarRealizacao() {
        // Rotina nao restringe a especialidade do profissional.
    }

    @Override
    public TipoConsulta getTipo() {
        return TipoConsulta.ROTINA;
    }

    @Override
    public Consulta reagendar(LocalDate novaData, LocalTime novoHorario) {
        return new ConsultaRotina(getPaciente(), getProfissionalResponsavel(), novaData, novoHorario, getMotivo(),
                getObservacoes());
    }
}
