package br.pucminas.hospital.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospital.dto.PacienteRequest;
import br.pucminas.hospital.dto.PacienteResponse;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.Cpf;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.repository.PacienteRepository;

@Service
@Transactional
public class PacienteService {

    private final PacienteRepository pacientes;

    public PacienteService(PacienteRepository pacientes) {
        this.pacientes = pacientes;
    }

    public PacienteResponse cadastrar(PacienteRequest req) {
        String cpf = Cpf.normalizar(req.cpf());
        if (pacientes.existsByCpf(cpf)) {
            throw cpfJaCadastrado(cpf);
        }
        Paciente novo = new Paciente(req.nome(), req.cpf(), req.dataNascimento(), req.telefone(), req.email(),
                req.endereco().paraEntidade());
        return PacienteResponse.de(pacientes.save(novo));
    }

    /** Todos os pacientes em ordem alfabetica ou, com nome, apenas os que contem o trecho informado. */
    @Transactional(readOnly = true)
    public List<PacienteResponse> listar(String nome) {
        Sort porNome = Sort.by("nome");
        List<Paciente> encontrados = (nome == null || nome.isBlank())
                ? pacientes.findAll(porNome)
                : pacientes.findByNomeContainingIgnoreCase(nome.trim(), porNome);
        return encontrados.stream().map(PacienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PacienteResponse buscar(Long id) {
        return PacienteResponse.de(obter(id));
    }

    public PacienteResponse atualizar(Long id, PacienteRequest req) {
        Paciente paciente = obter(id);
        String cpf = Cpf.normalizar(req.cpf());
        if (pacientes.existsByCpfAndIdNot(cpf, id)) {
            throw cpfJaCadastrado(cpf);
        }
        paciente.atualizarDados(req.nome(), req.cpf(), req.dataNascimento(), req.telefone(), req.email(),
                req.endereco().paraEntidade());
        return PacienteResponse.de(paciente);
    }

    /** RN6: o historico de atendimentos do paciente e mantido, entao quem ja foi atendido nao pode ser excluido. */
    public void remover(Long id) {
        Paciente paciente = obter(id);
        if (pacientes.possuiAtendimentos(id)) {
            throw new RegraNegocioException("Paciente possui atendimentos registrados e nao pode ser excluido");
        }
        pacientes.delete(paciente);
    }

    private RegraNegocioException cpfJaCadastrado(String cpf) {
        return new RegraNegocioException("Ja existe paciente com o CPF " + Cpf.formatar(cpf));
    }

    private Paciente obter(Long id) {
        return pacientes.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
    }
}
