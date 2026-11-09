package br.pucminas.hospital.model;

import br.pucminas.hospital.model.enums.Especialidade;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

// TODO(dono: Profissional): registro profissional, telefone, e-mail etc.
@Entity
public class ProfissionalSaude extends Pessoa {

    /** Coluna opcional ate a hierarquia de medicos assumir a especialidade. */
    @Enumerated(EnumType.STRING)
    private Especialidade especialidade;

    protected ProfissionalSaude() {
    }

    public ProfissionalSaude(String nome) {
        super(nome);
    }

    /** Falso quando o profissional nao tem a especialidade pedida. */
    public boolean atendeEspecialidade(Especialidade especialidade) {
        return especialidade != null && especialidade == this.especialidade;
    }

    public Especialidade getEspecialidade() { return especialidade; }
    public void setEspecialidade(Especialidade especialidade) { this.especialidade = especialidade; }
}
