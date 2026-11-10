package br.pucminas.hospital.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.model.enums.StatusConsulta;
import br.pucminas.hospital.model.enums.TipoConsulta;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** Atendimento ambulatorial. A situacao so muda pelas transicoes de {@link StatusConsulta}. */
@Entity
public abstract class Consulta extends Atendimento {

    private LocalDate data;

    private LocalTime horario;

    @Enumerated(EnumType.STRING)
    private StatusConsulta status;

    private String motivo;

    protected Consulta() {
    }

    protected Consulta(Paciente paciente, ProfissionalSaude profissional, LocalDate data, LocalTime horario,
            String motivo, String observacoesMedicas) {
        super(paciente, profissional);
        this.data = data;
        this.horario = horario;
        this.motivo = motivo;
        this.status = StatusConsulta.AGENDADA;
        definirObservacoes(observacoesMedicas);
    }

    /** Valida a modalidade antes de persistir o agendamento. A realizacao usa a mesma regra. */
    public void validarCompatibilidade() {
        validarRealizacao();
    }

    public void transitarPara(StatusConsulta destino) {
        if (status == null || !status.podeIrPara(destino)) {
            throw new RegraNegocioException("Transicao de status invalida: " + status + " -> " + destino);
        }
        this.status = destino;
    }

    /** CONFIRMADA -> EM_ATENDIMENTO. */
    public void iniciarAtendimento() {
        transitarPara(StatusConsulta.EM_ATENDIMENTO);
    }

    /** AGENDADA ou CONFIRMADA -> NAO_COMPARECEU. */
    public void marcarNaoComparecimento() {
        transitarPara(StatusConsulta.NAO_COMPARECEU);
    }

    /**
     * EM_ATENDIMENTO -> CONCLUIDA. A transicao e checada antes da realizacao, para que uma consulta
     * fora de atendimento nao tenha as observacoes alteradas; {@link #realizar(String)} valida a
     * modalidade e so entao o status muda.
     */
    public void concluir(String observacoes) {
        if (status == null || !status.podeIrPara(StatusConsulta.CONCLUIDA)) {
            throw new RegraNegocioException("Transicao de status invalida: " + status + " -> "
                    + StatusConsulta.CONCLUIDA);
        }
        realizar(observacoes);
        transitarPara(StatusConsulta.CONCLUIDA);
    }

    /** Nova consulta do mesmo subtipo. A original permanece no historico, cancelada pelo servico. */
    public abstract Consulta reagendar(LocalDate novaData, LocalTime novoHorario);

    public abstract TipoConsulta getTipo();

    public Especialidade getEspecialidadeSolicitada() {
        return null;
    }

    @Override
    public LocalDateTime getDataReferencia() {
        if (data == null || horario == null) {
            return null;
        }
        return LocalDateTime.of(data, horario);
    }

    public LocalDate getData() { return data; }
    public LocalTime getHorario() { return horario; }
    public StatusConsulta getStatus() { return status; }
    public String getMotivo() { return motivo; }
}
