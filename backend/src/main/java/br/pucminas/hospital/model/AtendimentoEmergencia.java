package br.pucminas.hospital.model;

import java.time.LocalDateTime;
import java.util.Comparator;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.Prioridade;
import br.pucminas.hospital.model.enums.StatusEmergencia;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Atendimento de pronto-socorro. Fluxo: registrar a chegada (AGUARDANDO_AVALIACAO), classificar a
 * prioridade na triagem, iniciar (EM_ATENDIMENTO) e concluir (CONCLUIDO). A prioridade pode faltar na
 * chegada, mas precisa estar definida para iniciar e para concluir o atendimento.
 */
@Entity
public class AtendimentoEmergencia extends Atendimento {

    /** Ordem da fila: prioridade mais grave primeiro; empate, quem chegou antes. */
    public static final Comparator<AtendimentoEmergencia> ORDEM_DA_FILA = Comparator
            .comparing((AtendimentoEmergencia e) -> e.getPrioridade() == null ? -1 : e.getPrioridade().getNivel())
            .reversed()
            .thenComparing(AtendimentoEmergencia::getDataHoraChegada);

    @Enumerated(EnumType.STRING)
    private Prioridade prioridade;

    @Enumerated(EnumType.STRING)
    private StatusEmergencia status;

    private String queixaPrincipal;

    private LocalDateTime dataHoraChegada;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;

    protected AtendimentoEmergencia() {
    }

    public AtendimentoEmergencia(Paciente paciente, ProfissionalSaude profissional, Prioridade prioridade,
            String queixaPrincipal, LocalDateTime dataHoraChegada) {
        super(paciente, profissional);
        if (queixaPrincipal == null || queixaPrincipal.isBlank()) {
            throw new RegraNegocioException("Queixa principal e obrigatoria");
        }
        if (dataHoraChegada == null) {
            throw new RegraNegocioException("Data e hora de chegada sao obrigatorias");
        }
        this.prioridade = prioridade;
        this.queixaPrincipal = queixaPrincipal.trim();
        this.dataHoraChegada = dataHoraChegada;
        this.status = StatusEmergencia.AGUARDANDO_AVALIACAO;
    }

    /** Triagem: define ou reclassifica a prioridade enquanto o atendimento nao terminou. */
    public void classificar(Prioridade novaPrioridade) {
        if (novaPrioridade == null) {
            throw new RegraNegocioException("Prioridade e obrigatoria");
        }
        if (status.isFinal()) {
            throw new RegraNegocioException("Atendimento de emergencia ja concluido nao pode ser reclassificado");
        }
        this.prioridade = novaPrioridade;
    }

    public void iniciarAtendimento(LocalDateTime momento) {
        exigirPrioridade();
        transitarPara(StatusEmergencia.EM_ATENDIMENTO);
        this.dataHoraInicio = momento;
    }

    /** Usa o Template Method {@link #realizar(String)}: valida a prioridade antes de concluir. */
    public void concluir(String observacoes, LocalDateTime momento) {
        if (!status.podeIrPara(StatusEmergencia.CONCLUIDO)) {
            throw new RegraNegocioException("Atendimento de emergencia precisa estar EM_ATENDIMENTO para ser concluido"
                    + " (status atual: " + status + ")");
        }
        realizar(observacoes);
        this.status = StatusEmergencia.CONCLUIDO;
        this.dataHoraFim = momento;
    }

    /** Regra da modalidade: a prioridade precisa estar definida. */
    @Override
    protected void validarRealizacao() {
        exigirPrioridade();
    }

    private void exigirPrioridade() {
        if (prioridade == null) {
            throw new RegraNegocioException("Atendimento de emergencia exige prioridade definida (faca a triagem)");
        }
    }

    private void transitarPara(StatusEmergencia destino) {
        if (status == null || !status.podeIrPara(destino)) {
            throw new RegraNegocioException("Transicao de status invalida: " + status + " -> " + destino);
        }
        this.status = destino;
    }

    public boolean isAguardando() {
        return status == StatusEmergencia.AGUARDANDO_AVALIACAO;
    }

    @Override
    public LocalDateTime getDataReferencia() {
        return dataHoraChegada;
    }

    public Prioridade getPrioridade() { return prioridade; }
    public StatusEmergencia getStatus() { return status; }
    public String getQueixaPrincipal() { return queixaPrincipal; }
    public LocalDateTime getDataHoraChegada() { return dataHoraChegada; }
    public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
    public LocalDateTime getDataHoraFim() { return dataHoraFim; }
}
