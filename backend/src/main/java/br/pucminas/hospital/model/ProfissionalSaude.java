package br.pucminas.hospital.model;

import jakarta.persistence.Entity;

// TODO(dono: Profissional): registro profissional, telefone, e-mail etc.
@Entity
public class ProfissionalSaude extends Pessoa {

    protected ProfissionalSaude() {
    }

    public ProfissionalSaude(String nome) {
        super(nome);
    }
}
