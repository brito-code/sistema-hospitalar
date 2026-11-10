package br.pucminas.hospital.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospital.dto.ConclusaoRequest;
import br.pucminas.hospital.dto.ProcedimentoRequest;
import br.pucminas.hospital.dto.ProcedimentoResponse;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProcedimentoCirurgico;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.model.enums.StatusProcedimento;
import br.pucminas.hospital.repository.ConsultaRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProcedimentoCirurgicoRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;

@Service
@Transactional
public class ProcedimentoService {

    private final ProcedimentoCirurgicoRepository procedimentos;
    private final ConsultaRepository consultas;
    private final PacienteRepository pacientes;
    private final ProfissionalSaudeRepository profissionais;

    public ProcedimentoService(ProcedimentoCirurgicoRepository procedimentos, ConsultaRepository consultas,
            PacienteRepository pacientes, ProfissionalSaudeRepository profissionais) {
        this.procedimentos = procedimentos;
        this.consultas = consultas;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
    }

    public ProcedimentoResponse agendar(ProcedimentoRequest req) {
        Paciente paciente = pacientes.findById(req.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", req.pacienteId()));
        ProfissionalSaude profissional = profissionais.findById(req.profissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", req.profissionalId()));

        // o construtor ja rejeita responsavel que nao e cirurgiao
        ProcedimentoCirurgico procedimento = new ProcedimentoCirurgico(paciente, profissional, req.descricao(),
                req.dataHoraAgendada());
        garantirHorarioLivre(req.profissionalId(), req.dataHoraAgendada());
        return ProcedimentoResponse.de(procedimentos.save(procedimento));
    }

    @Transactional(readOnly = true)
    public List<ProcedimentoResponse> listar(Long profissionalId, Long pacienteId, StatusProcedimento status) {
        return procedimentos.filtrar(profissionalId, pacienteId, status).stream()
                .map(ProcedimentoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProcedimentoResponse buscar(Long id) {
        return ProcedimentoResponse.de(obter(id));
    }

    public ProcedimentoResponse iniciar(Long id) {
        ProcedimentoCirurgico procedimento = obter(id);
        procedimento.iniciar(LocalDateTime.now());
        return ProcedimentoResponse.de(procedimento);
    }

    public ProcedimentoResponse concluir(Long id, ConclusaoRequest req) {
        ProcedimentoCirurgico procedimento = obter(id);
        procedimento.concluir(req != null ? req.observacoes() : null, LocalDateTime.now());
        return ProcedimentoResponse.de(procedimento);
    }

    public ProcedimentoResponse cancelar(Long id) {
        ProcedimentoCirurgico procedimento = obter(id);
        procedimento.cancelar();
        return ProcedimentoResponse.de(procedimento);
    }

    /** RN3: o cirurgiao nao pode ter outro procedimento nem uma consulta no mesmo dia e horario. */
    private void garantirHorarioLivre(Long profissionalId, LocalDateTime dataHora) {
        boolean conflito = procedimentos.existeConflito(profissionalId, dataHora)
                || consultas.existeConflito(profissionalId, dataHora.toLocalDate(), dataHora.toLocalTime());
        if (conflito) {
            throw new RegraNegocioException("Profissional ja possui atendimento neste dia e horario");
        }
    }

    private ProcedimentoCirurgico obter(Long id) {
        return procedimentos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento cirurgico", id));
    }
}
