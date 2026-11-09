package br.pucminas.hospital.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Dados comuns a todos os atendimentos. Cada modalidade valida suas
 * proprias condicoes em {@link #validarRealizacao()} (Template Method).
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id")
    private ProfissionalSaude profissionalResponsavel;

    private String observacoes;

    protected Atendimento() {
    }

    protected Atendimento(Paciente paciente, ProfissionalSaude profissionalResponsavel) {
        this.paciente = paciente;
        this.profissionalResponsavel = profissionalResponsavel;
    }

    /** Template Method: valida as condicoes da modalidade e so entao registra a realizacao. */
    public final void realizar(String observacoes) {
        validarRealizacao();
        if (observacoes != null) {
            this.observacoes = observacoes;
        }
    }

    protected abstract void validarRealizacao();

    public abstract LocalDateTime getDataReferencia();

    /** Observacoes registradas no agendamento, sem passar pela realizacao do atendimento. */
    protected void definirObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public ProfissionalSaude getProfissionalResponsavel() { return profissionalResponsavel; }
    public String getObservacoes() { return observacoes; }
}
