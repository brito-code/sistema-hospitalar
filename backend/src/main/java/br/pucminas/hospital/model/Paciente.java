package br.pucminas.hospital.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

// TODO(dono: Paciente): atributos, regras e mapeamento de endereco/historico.
@Entity
public class Paciente extends Pessoa {

    @Transient
    private Endereco endereco;

    @Transient
    private HistoricoMedico historicoMedico;

    protected Paciente() {
    }

    public Paciente(String nome) {
        super(nome);
    }
}
