package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.model.enums.TipoProfissional;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Medico generalista: atende consultas de rotina e encaminha pacientes a especialistas. */
@Entity
@DiscriminatorValue(TipoProfissional.Valor.CLINICO_GERAL)
public class ClinicoGeral extends Medico {

    protected ClinicoGeral() {
    }

    public ClinicoGeral(String nome, String crm) {
        super(nome, crm);
    }

    /** Encaminha o paciente para uma especialidade. Quem vai atender e decidido pelo servico. */
    public Encaminhamento encaminhar(Paciente paciente, Especialidade especialidade) {
        if (paciente == null) {
            throw new RegraNegocioException("Encaminhamento exige paciente");
        }
        if (especialidade == null) {
            throw new RegraNegocioException("Encaminhamento exige especialidade");
        }
        return new Encaminhamento(this, paciente, especialidade);
    }

    @Override
    public TipoProfissional getTipo() {
        return TipoProfissional.CLINICO_GERAL;
    }
}
