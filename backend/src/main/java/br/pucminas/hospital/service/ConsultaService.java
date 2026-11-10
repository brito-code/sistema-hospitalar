package br.pucminas.hospital.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospital.dto.ConsultaRequest;
import br.pucminas.hospital.dto.ConsultaResponse;
import br.pucminas.hospital.dto.ReagendarRequest;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.Consulta;
import br.pucminas.hospital.model.ConsultaEspecializada;
import br.pucminas.hospital.model.ConsultaRotina;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.model.enums.StatusConsulta;
import br.pucminas.hospital.model.enums.TipoConsulta;
import br.pucminas.hospital.repository.ConsultaRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;

@Service
@Transactional
public class ConsultaService {

    private final ConsultaRepository consultas;
    private final PacienteRepository pacientes;
    private final ProfissionalSaudeRepository profissionais;

    public ConsultaService(ConsultaRepository consultas, PacienteRepository pacientes,
            ProfissionalSaudeRepository profissionais) {
        this.consultas = consultas;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
    }

    public ConsultaResponse agendar(ConsultaRequest req) {
        Paciente paciente = pacientes.findById(req.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", req.pacienteId()));
        ProfissionalSaude profissional = profissionais.findById(req.profissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", req.profissionalId()));

        Consulta consulta = criar(req, paciente, profissional);
        consulta.validarCompatibilidade();
        garantirHorarioLivre(req.profissionalId(), req.data(), req.horario());
        return ConsultaResponse.de(consultas.save(consulta));
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> listar(Long profissionalId, Long pacienteId, StatusConsulta status, LocalDate data) {
        return consultas.filtrar(profissionalId, pacienteId, status, data).stream().map(ConsultaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ConsultaResponse buscar(Long id) {
        return ConsultaResponse.de(obter(id));
    }

    public ConsultaResponse confirmar(Long id) {
        Consulta consulta = obter(id);
        consulta.transitarPara(StatusConsulta.CONFIRMADA);
        return ConsultaResponse.de(consulta);
    }

    public ConsultaResponse cancelar(Long id) {
        Consulta consulta = obter(id);
        consulta.transitarPara(StatusConsulta.CANCELADA);
        return ConsultaResponse.de(consulta);
    }

    public ConsultaResponse iniciar(Long id) {
        Consulta consulta = obter(id);
        consulta.iniciarAtendimento();
        return ConsultaResponse.de(consulta);
    }

    public ConsultaResponse concluir(Long id, String observacoes) {
        Consulta consulta = obter(id);
        consulta.concluir(observacoes);
        return ConsultaResponse.de(consulta);
    }

    public ConsultaResponse naoCompareceu(Long id) {
        Consulta consulta = obter(id);
        consulta.marcarNaoComparecimento();
        return ConsultaResponse.de(consulta);
    }

    /**
     * Cancela a consulta original e grava outro agendamento. A vaga da original
     * deixa de bloquear o horario porque o conflito ignora canceladas.
     */
    public ConsultaResponse reagendar(Long id, ReagendarRequest req) {
        Consulta original = obter(id);
        original.transitarPara(StatusConsulta.CANCELADA);
        consultas.saveAndFlush(original);
        garantirHorarioLivre(original.getProfissionalResponsavel().getId(), req.novaData(), req.novoHorario());
        Consulta nova = original.reagendar(req.novaData(), req.novoHorario());
        return ConsultaResponse.de(consultas.save(nova));
    }

    private Consulta criar(ConsultaRequest req, Paciente paciente, ProfissionalSaude profissional) {
        if (req.tipo() == TipoConsulta.ESPECIALIZADA) {
            if (req.especialidade() == null) {
                throw new RegraNegocioException("Consulta especializada exige especialidade");
            }
            return new ConsultaEspecializada(paciente, profissional, req.data(), req.horario(), req.motivo(),
                    req.observacoesMedicas(), req.especialidade());
        }
        return new ConsultaRotina(paciente, profissional, req.data(), req.horario(), req.motivo(),
                req.observacoesMedicas());
    }

    private void garantirHorarioLivre(Long profissionalId, LocalDate data, LocalTime horario) {
        if (consultas.existeConflito(profissionalId, data, horario)) {
            throw new RegraNegocioException("Profissional ja possui atendimento neste dia e horario");
        }
    }

    private Consulta obter(Long id) {
        return consultas.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Consulta", id));
    }
}
