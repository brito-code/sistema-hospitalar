package br.pucminas.hospital.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospital.dto.EncaminhamentoRequest;
import br.pucminas.hospital.dto.EncaminhamentoResponse;
import br.pucminas.hospital.dto.ProfissionalRequest;
import br.pucminas.hospital.dto.ProfissionalRequest.CirurgiaoRequest;
import br.pucminas.hospital.dto.ProfissionalRequest.ClinicoGeralRequest;
import br.pucminas.hospital.dto.ProfissionalRequest.EspecialistaRequest;
import br.pucminas.hospital.dto.ProfissionalResponse;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.Cirurgiao;
import br.pucminas.hospital.model.ClinicoGeral;
import br.pucminas.hospital.model.Encaminhamento;
import br.pucminas.hospital.model.MedicoEspecialista;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;

@Service
@Transactional
public class ProfissionalService {

    private final ProfissionalSaudeRepository profissionais;
    private final PacienteRepository pacientes;

    public ProfissionalService(ProfissionalSaudeRepository profissionais, PacienteRepository pacientes) {
        this.profissionais = profissionais;
        this.pacientes = pacientes;
    }

    public ProfissionalResponse cadastrar(ProfissionalRequest req) {
        String registro = normalizarRegistro(req.registroProfissional());
        if (profissionais.existsByRegistroProfissionalIgnoreCase(registro)) {
            throw new RegraNegocioException("Ja existe profissional com o registro " + registro);
        }
        ProfissionalSaude novo = criar(req, registro);
        novo.setTelefone(req.telefone());
        novo.setEmail(req.email());
        return ProfissionalResponse.de(profissionais.save(novo));
    }

    /** Lista todos ou, com especialidade, apenas os especialistas que a atendem. */
    @Transactional(readOnly = true)
    public List<ProfissionalResponse> listar(Especialidade especialidade) {
        List<? extends ProfissionalSaude> encontrados = especialidade == null
                ? profissionais.findAll().stream().sorted(Comparator.comparing(ProfissionalSaude::getNome)).toList()
                : profissionais.buscarEspecialistas(especialidade);
        return encontrados.stream().map(ProfissionalResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProfissionalResponse buscar(Long id) {
        return ProfissionalResponse.de(obter(id));
    }

    public ProfissionalResponse atualizar(Long id, ProfissionalRequest req) {
        ProfissionalSaude profissional = obter(id);
        if (profissional.getTipo() != req.tipo()) {
            throw new RegraNegocioException("O tipo do profissional nao pode ser alterado");
        }
        String registro = normalizarRegistro(req.registroProfissional());
        if (profissionais.existsByRegistroProfissionalIgnoreCaseAndIdNot(registro, id)) {
            throw new RegraNegocioException("Ja existe profissional com o registro " + registro);
        }
        profissional.atualizarDados(req.nome(), registro, req.telefone(), req.email());
        if (profissional instanceof MedicoEspecialista especialista && req instanceof EspecialistaRequest r) {
            especialista.setEspecialidade(r.especialidade());
        } else if (profissional instanceof Cirurgiao cirurgiao && req instanceof CirurgiaoRequest r) {
            cirurgiao.setAreaCirurgica(r.areaCirurgica());
        }
        return ProfissionalResponse.de(profissional);
    }

    /** RN6/RN7: profissional com historico de atendimentos nao pode ser excluido. */
    public void remover(Long id) {
        ProfissionalSaude profissional = obter(id);
        if (profissionais.possuiAtendimentos(id)) {
            throw new RegraNegocioException("Profissional possui atendimentos registrados e nao pode ser excluido");
        }
        profissionais.delete(profissional);
    }

    /** Somente o clinico geral encaminha; o paciente vai para os especialistas da especialidade. */
    @Transactional(readOnly = true)
    public EncaminhamentoResponse encaminhar(Long clinicoId, EncaminhamentoRequest req) {
        ProfissionalSaude profissional = obter(clinicoId);
        if (!(profissional instanceof ClinicoGeral clinico)) {
            throw new RegraNegocioException("Somente clinico geral pode encaminhar pacientes");
        }
        Paciente paciente = pacientes.findById(req.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", req.pacienteId()));

        Encaminhamento encaminhamento = clinico.encaminhar(paciente, req.especialidade());
        List<MedicoEspecialista> especialistas = profissionais.buscarEspecialistas(req.especialidade());
        if (especialistas.isEmpty()) {
            throw new RegraNegocioException("Nenhum especialista disponivel em " + req.especialidade());
        }
        return EncaminhamentoResponse.de(encaminhamento, especialistas);
    }

    private ProfissionalSaude criar(ProfissionalRequest req, String registro) {
        if (req instanceof EspecialistaRequest r) {
            return new MedicoEspecialista(r.nome(), registro, r.especialidade());
        }
        if (req instanceof CirurgiaoRequest r) {
            return new Cirurgiao(r.nome(), registro, r.areaCirurgica());
        }
        if (req instanceof ClinicoGeralRequest r) {
            return new ClinicoGeral(r.nome(), registro);
        }
        throw new RegraNegocioException("Tipo de profissional nao suportado");
    }

    private static String normalizarRegistro(String registro) {
        return registro == null ? null : registro.trim().toUpperCase(Locale.ROOT);
    }

    private ProfissionalSaude obter(Long id) {
        ProfissionalSaude profissional = profissionais.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", id));
        return (ProfissionalSaude) Hibernate.unproxy(profissional);
    }
}
