package br.pucminas.hospital.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospital.dto.ClassificacaoRequest;
import br.pucminas.hospital.dto.ConclusaoRequest;
import br.pucminas.hospital.dto.EmergenciaRequest;
import br.pucminas.hospital.dto.EmergenciaResponse;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.model.AtendimentoEmergencia;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.model.enums.StatusEmergencia;
import br.pucminas.hospital.repository.AtendimentoEmergenciaRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;

@Service
@Transactional
public class EmergenciaService {

    private final AtendimentoEmergenciaRepository emergencias;
    private final PacienteRepository pacientes;
    private final ProfissionalSaudeRepository profissionais;

    public EmergenciaService(AtendimentoEmergenciaRepository emergencias, PacienteRepository pacientes,
            ProfissionalSaudeRepository profissionais) {
        this.emergencias = emergencias;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
    }

    public EmergenciaResponse registrar(EmergenciaRequest req) {
        Paciente paciente = pacientes.findById(req.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", req.pacienteId()));
        ProfissionalSaude profissional = profissionais.findById(req.profissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", req.profissionalId()));
        LocalDateTime chegada = req.dataHoraChegada() != null ? req.dataHoraChegada() : LocalDateTime.now();

        AtendimentoEmergencia emergencia = new AtendimentoEmergencia(paciente, profissional, req.prioridade(),
                req.queixaPrincipal(), chegada);
        return EmergenciaResponse.de(emergencias.save(emergencia));
    }

    @Transactional(readOnly = true)
    public List<EmergenciaResponse> listar(Long pacienteId, StatusEmergencia status) {
        return emergencias.filtrar(pacienteId, status).stream().map(EmergenciaResponse::de).toList();
    }

    /** Fila do pronto-socorro: so quem aguarda avaliacao, da prioridade mais grave para a mais leve. */
    @Transactional(readOnly = true)
    public List<EmergenciaResponse> listarAguardando() {
        return emergencias.findByStatus(StatusEmergencia.AGUARDANDO_AVALIACAO).stream()
                .sorted(AtendimentoEmergencia.ORDEM_DA_FILA)
                .map(EmergenciaResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmergenciaResponse buscar(Long id) {
        return EmergenciaResponse.de(obter(id));
    }

    public EmergenciaResponse classificar(Long id, ClassificacaoRequest req) {
        AtendimentoEmergencia emergencia = obter(id);
        emergencia.classificar(req.prioridade());
        return EmergenciaResponse.de(emergencia);
    }

    public EmergenciaResponse iniciar(Long id) {
        AtendimentoEmergencia emergencia = obter(id);
        emergencia.iniciarAtendimento(LocalDateTime.now());
        return EmergenciaResponse.de(emergencia);
    }

    public EmergenciaResponse concluir(Long id, ConclusaoRequest req) {
        AtendimentoEmergencia emergencia = obter(id);
        emergencia.concluir(req != null ? req.observacoes() : null, LocalDateTime.now());
        return EmergenciaResponse.de(emergencia);
    }

    private AtendimentoEmergencia obter(Long id) {
        return emergencias.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento de emergencia", id));
    }
}
