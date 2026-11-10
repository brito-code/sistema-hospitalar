package br.pucminas.hospital.model;

import jakarta.persistence.Entity;

/** Profissional medico (CRM). Base de clinico geral, especialista e cirurgiao. */
@Entity
public abstract class Medico extends ProfissionalSaude {

    protected Medico() {
    }

    protected Medico(String nome, String crm) {
        super(nome, crm);
    }
}
