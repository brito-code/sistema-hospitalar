package br.pucminas.hospital.model;

import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.model.enums.TipoProfissional;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** Medico com uma especialidade; e quem atende consultas especializadas. */
@Entity
@DiscriminatorValue(TipoProfissional.Valor.ESPECIALISTA)
public class MedicoEspecialista extends Medico {

    @Enumerated(EnumType.STRING)
    private Especialidade especialidade;

    protected MedicoEspecialista() {
    }

    public MedicoEspecialista(String nome, String crm, Especialidade especialidade) {
        super(nome, crm);
        this.especialidade = especialidade;
    }

    public boolean atende(Especialidade especialidade) {
        return this.especialidade != null && this.especialidade == especialidade;
    }

    @Override
    public boolean atendeEspecialidade(Especialidade especialidade) {
        return atende(especialidade);
    }

    @Override
    public Especialidade getEspecialidade() { return especialidade; }

    public void setEspecialidade(Especialidade especialidade) { this.especialidade = especialidade; }

    @Override
    public TipoProfissional getTipo() {
        return TipoProfissional.ESPECIALISTA;
    }
}
