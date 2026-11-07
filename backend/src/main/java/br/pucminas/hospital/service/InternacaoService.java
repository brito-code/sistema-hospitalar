package br.pucminas.hospital.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospital.dto.AltaRequest;
import br.pucminas.hospital.dto.InternacaoRequest;
import br.pucminas.hospital.dto.InternacaoResponse;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.model.Internacao;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.model.Quarto;
import br.pucminas.hospital.repository.InternacaoRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;
import br.pucminas.hospital.repository.QuartoRepository;

@Service
@Transactional
public class InternacaoService {

    private final InternacaoRepository internacoes;
    private final QuartoRepository quartos;
    private final PacienteRepository pacientes;
    private final ProfissionalSaudeRepository profissionais;

    public InternacaoService(InternacaoRepository internacoes, QuartoRepository quartos,
            PacienteRepository pacientes, ProfissionalSaudeRepository profissionais) {
        this.internacoes = internacoes;
        this.quartos = quartos;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
    }

    public InternacaoResponse internar(InternacaoRequest req) {
        Paciente paciente = pacientes.findById(req.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", req.pacienteId()));
        ProfissionalSaude profissional = profissionais.findById(req.profissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", req.profissionalId()));
        Quarto quarto = quartos.buscarComTrava(req.quartoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto", req.quartoId()));

        Internacao internacao = new Internacao(paciente, profissional, quarto, req.dataEntrada(), req.dataPrevistaAlta());
        internacao.realizar(req.observacoes()); // valida vaga (RN4/RN5)
        internacao.alocarNoQuarto();
        return InternacaoResponse.de(internacoes.save(internacao));
    }

    @Transactional(readOnly = true)
    public List<InternacaoResponse> listar() {
        return internacoes.findAll().stream().map(InternacaoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public InternacaoResponse buscar(Long id) {
        return InternacaoResponse.de(obter(id));
    }

    public InternacaoResponse darAlta(Long id, AltaRequest req) {
        Internacao internacao = obter(id);
        LocalDate data = (req != null && req.dataEfetivaAlta() != null) ? req.dataEfetivaAlta() : LocalDate.now();
        internacao.darAlta(data); // a vaga e liberada porque a ocupacao conta so internacoes ativas
        return InternacaoResponse.de(internacao);
    }

    private Internacao obter(Long id) {
        return internacoes.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Internacao", id));
    }
}
