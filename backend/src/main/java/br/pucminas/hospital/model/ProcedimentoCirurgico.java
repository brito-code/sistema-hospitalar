package br.pucminas.hospital.model;

import java.time.LocalDateTime;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.StatusProcedimento;
import br.pucminas.hospital.model.enums.TipoProfissional;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Cirurgia agendada para um paciente. Fluxo: AGENDADO -> EM_ANDAMENTO -> CONCLUIDO, ou AGENDADO -> CANCELADO.
 * O responsavel precisa ser {@link Cirurgiao} e o procedimento so e concluido depois de iniciado.
 */
@Entity
public class ProcedimentoCirurgico extends Atendimento {

    private String descricao;

    private String tipoCirurgia;

    private String observacoesPreOperatorias;
    private String observacoesPosOperatorias;

    /** Previstos (agendamento) e efetivos (inicio e fim reais). */
    private LocalDateTime dataHoraPrevista;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;

    @Enumerated(EnumType.STRING)
    private StatusProcedimento status;

    protected ProcedimentoCirurgico() {
    }

    public ProcedimentoCirurgico(Paciente paciente, ProfissionalSaude responsavel, String descricao,
            String tipoCirurgia, LocalDateTime dataHoraPrevista, String observacoesPreOperatorias) {
        super(paciente, responsavel);
        if (descricao == null || descricao.isBlank()) {
            throw new RegraNegocioException("Descricao do procedimento e obrigatoria");
        }
        if (tipoCirurgia == null || tipoCirurgia.isBlank()) {
            throw new RegraNegocioException("Tipo de cirurgia e obrigatorio");
        }
        if (dataHoraPrevista == null) {
            throw new RegraNegocioException("Data e hora do procedimento sao obrigatorias");
        }
        this.descricao = descricao.trim();
        this.tipoCirurgia = tipoCirurgia.trim();
        this.dataHoraPrevista = dataHoraPrevista;
        this.observacoesPreOperatorias = observacoesPreOperatorias;
        this.status = StatusProcedimento.AGENDADO;
        exigirCirurgiao(); // falha ja no agendamento, nao so na conclusao
    }

    public void iniciar(LocalDateTime momento) {
        exigirCirurgiao();
        transitarPara(StatusProcedimento.EM_ANDAMENTO);
        this.dataHoraInicio = momento;
    }

    /** Usa o Template Method {@link #realizar(String)}: so conclui se a validacao da modalidade passar. */
    public void concluir(String observacoesPosOperatorias, LocalDateTime momento) {
        realizar(observacoesPosOperatorias);
        this.observacoesPosOperatorias = observacoesPosOperatorias;
        this.status = StatusProcedimento.CONCLUIDO;
        this.dataHoraFim = momento;
    }

    public void cancelar() {
        transitarPara(StatusProcedimento.CANCELADO);
    }

    /** Regras da modalidade: responsavel cirurgiao e procedimento ja iniciado. */
    @Override
    protected void validarRealizacao() {
        exigirCirurgiao();
        if (status != StatusProcedimento.EM_ANDAMENTO) {
            throw new RegraNegocioException("Procedimento precisa ser iniciado antes de ser concluido"
                    + " (status atual: " + status + ")");
        }
    }

    /**
     * Usa getTipo() em vez de instanceof: o responsavel carregado do banco pode ser um proxy do Hibernate
     * (ProfissionalSaude), e instanceof Cirurgiao falharia nesse caso. A chamada do metodo chega no objeto real.
     */
    private void exigirCirurgiao() {
        ProfissionalSaude responsavel = getProfissionalResponsavel();
        if (responsavel == null || responsavel.getTipo() != TipoProfissional.CIRURGIAO) {
            throw new RegraNegocioException("Responsavel pelo procedimento cirurgico precisa ser um cirurgiao");
        }
    }

    private void transitarPara(StatusProcedimento destino) {
        if (status == null || !status.podeIrPara(destino)) {
            throw new RegraNegocioException("Transicao de status invalida: " + status + " -> " + destino);
        }
        this.status = destino;
    }

    @Override
    public LocalDateTime getDataReferencia() {
        return dataHoraPrevista;
    }

    public String getDescricao() { return descricao; }
    public String getTipoCirurgia() { return tipoCirurgia; }
    public String getObservacoesPreOperatorias() { return observacoesPreOperatorias; }
    public String getObservacoesPosOperatorias() { return observacoesPosOperatorias; }
    public LocalDateTime getDataHoraPrevista() { return dataHoraPrevista; }
    public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
    public LocalDateTime getDataHoraFim() { return dataHoraFim; }
    public StatusProcedimento getStatus() { return status; }
}
