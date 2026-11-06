package br.pucminas.hospital.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import br.pucminas.hospital.exception.AltaInvalidaException;
import br.pucminas.hospital.exception.QuartoSemVagaException;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Internacao extends Atendimento {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quarto_id")
    private Quarto quarto;

    private LocalDate dataEntrada;
    private LocalDate dataPrevistaAlta;
    private LocalDate dataEfetivaAlta;

    protected Internacao() {
    }

    public Internacao(Paciente paciente, ProfissionalSaude profissional, Quarto quarto,
            LocalDate dataEntrada, LocalDate dataPrevistaAlta) {
        super(paciente, profissional);
        this.quarto = quarto;
        this.dataEntrada = dataEntrada;
        this.dataPrevistaAlta = dataPrevistaAlta;
    }

    /** Registra a internacao no quarto. Chamar depois de {@link #realizar(String)}. */
    public void alocarNoQuarto() {
        quarto.adicionarInternacao(this);
    }

    /** RN4/RN5: o quarto precisa ter vaga. */
    @Override
    protected void validarRealizacao() {
        if (!quarto.temVaga()) {
            throw new QuartoSemVagaException(quarto.getId());
        }
    }

    @Override
    public LocalDateTime getDataReferencia() {
        return dataEntrada.atStartOfDay();
    }

    public void darAlta(LocalDate dataAlta) {
        if (dataEfetivaAlta != null) {
            throw new AltaInvalidaException("Internacao ja recebeu alta em " + dataEfetivaAlta);
        }
        if (dataAlta == null) {
            throw new AltaInvalidaException("Data de alta e obrigatoria");
        }
        if (dataAlta.isBefore(dataEntrada)) {
            throw new AltaInvalidaException("Data de alta nao pode ser anterior a data de entrada");
        }
        this.dataEfetivaAlta = dataAlta;
    }

    public boolean isAtiva() {
        return dataEfetivaAlta == null;
    }

    public long getDiasInternado() {
        LocalDate fim = dataEfetivaAlta != null ? dataEfetivaAlta : LocalDate.now();
        return ChronoUnit.DAYS.between(dataEntrada, fim);
    }

    public Quarto getQuarto() { return quarto; }
    public LocalDate getDataEntrada() { return dataEntrada; }
    public LocalDate getDataPrevistaAlta() { return dataPrevistaAlta; }
    public LocalDate getDataEfetivaAlta() { return dataEfetivaAlta; }
}
