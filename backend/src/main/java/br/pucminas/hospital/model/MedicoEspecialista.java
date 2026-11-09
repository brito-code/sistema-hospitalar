package br.pucminas.hospital.model;

import br.pucminas.hospital.model.enums.Especialidade;

// TODO(dono: Profissional): mapeamento JPA da hierarquia. A especialidade ja responde a consulta.
public class MedicoEspecialista extends Medico {

    private Especialidade especialidade;

    protected MedicoEspecialista() {
    }

    public MedicoEspecialista(String nome, Especialidade especialidade) {
        super(nome);
        this.especialidade = especialidade;
    }

    @Override
    public boolean atendeEspecialidade(Especialidade especialidade) {
        return this.especialidade != null && this.especialidade == especialidade;
    }

    @Override
    public Especialidade getEspecialidade() { return especialidade; }
}
