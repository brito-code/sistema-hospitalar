package br.pucminas.hospital.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospital.GeradorDeCpf;
import br.pucminas.hospital.model.Atendimento;
import br.pucminas.hospital.model.ClinicoGeral;
import br.pucminas.hospital.model.ConsultaRotina;
import br.pucminas.hospital.model.Endereco;
import br.pucminas.hospital.model.Internacao;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.model.Quarto;
import br.pucminas.hospital.model.enums.TipoQuarto;
import br.pucminas.hospital.repository.ConsultaRepository;
import br.pucminas.hospital.repository.InternacaoRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;
import br.pucminas.hospital.repository.QuartoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Regra 1 do enunciado: um paciente pode ter varias consultas e internacoes ao longo do tempo.
 * Roda numa transacao que e desfeita no fim, entao nao deixa dados no banco compartilhado de teste.
 */
@SpringBootTest
@Transactional
class PacienteHistoricoTest {

    @PersistenceContext EntityManager em;
    @Autowired PacienteRepository pacientes;
    @Autowired ProfissionalSaudeRepository profissionais;
    @Autowired ConsultaRepository consultas;
    @Autowired InternacaoRepository internacoes;
    @Autowired QuartoRepository quartos;

    private Paciente novoPaciente(String nome) {
        Endereco endereco = new Endereco("Rua da Bahia", "1200", null, "Centro", "Belo Horizonte", "MG", "30160011");
        return pacientes.save(new Paciente(nome, GeradorDeCpf.proximo(), LocalDate.of(1988, 3, 14),
                "(31) 98812-3401", "historico@email.com", endereco));
    }

    @Test
    void pacienteAcumulaConsultasEInternacoesSemMisturarComOutrosPacientes() {
        Paciente paciente = novoPaciente("Ana Clara Mendes");
        Paciente outro = novoPaciente("Bruno Costa");
        ProfissionalSaude medico = profissionais.save(new ClinicoGeral("Dr. Historico", "CRM-MG 800002"));
        Quarto quarto = quartos.save(new Quarto("H-101", 1, 4, TipoQuarto.ENFERMARIA));

        consultas.save(new ConsultaRotina(paciente, medico, LocalDate.of(2025, 3, 10), LocalTime.of(9, 0),
                "avaliacao inicial", null));
        consultas.save(new ConsultaRotina(paciente, medico, LocalDate.of(2026, 1, 20), LocalTime.of(14, 30),
                "retorno", null));
        internacoes.save(new Internacao(paciente, medico, quarto, LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 5)));
        internacoes.save(new Internacao(paciente, medico, quarto, LocalDate.of(2026, 2, 3), null));
        consultas.save(new ConsultaRotina(outro, medico, LocalDate.of(2026, 1, 20), LocalTime.of(16, 0),
                "rotina", null));

        em.flush();
        em.clear();

        Paciente recarregado = pacientes.findById(paciente.getId()).orElseThrow();
        assertEquals(2, recarregado.getConsultas().size());
        assertEquals(2, recarregado.getInternacoes().size());
        assertEquals(4, recarregado.getAtendimentos().size());
        assertEquals(1, pacientes.findById(outro.getId()).orElseThrow().getAtendimentos().size());
    }

    @Test
    void historicoSaiEmOrdemCronologica() {
        Paciente paciente = novoPaciente("Camila Souza");
        ProfissionalSaude medico = profissionais.save(new ClinicoGeral("Dr. Historico", "CRM-MG 800003"));
        Quarto quarto = quartos.save(new Quarto("H-102", 1, 4, TipoQuarto.ENFERMARIA));

        // gravados fora de ordem de proposito
        internacoes.save(new Internacao(paciente, medico, quarto, LocalDate.of(2026, 2, 3), null));
        consultas.save(new ConsultaRotina(paciente, medico, LocalDate.of(2026, 1, 20), LocalTime.of(14, 30),
                "retorno", null));
        internacoes.save(new Internacao(paciente, medico, quarto, LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 5)));
        consultas.save(new ConsultaRotina(paciente, medico, LocalDate.of(2025, 3, 10), LocalTime.of(9, 0),
                "avaliacao inicial", null));

        em.flush();
        em.clear();

        List<LocalDateTime> datas = pacientes.findById(paciente.getId()).orElseThrow().getAtendimentos().stream()
                .map(Atendimento::getDataReferencia)
                .toList();

        assertEquals(List.of(
                LocalDateTime.of(2025, 3, 10, 9, 0),
                LocalDateTime.of(2025, 8, 1, 0, 0),
                LocalDateTime.of(2026, 1, 20, 14, 30),
                LocalDateTime.of(2026, 2, 3, 0, 0)), datas);
    }

    @Test
    void repositorioInformaSePacienteTemAtendimentos() {
        Paciente comHistorico = novoPaciente("Diego Alves");
        Paciente semHistorico = novoPaciente("Elisa Prado");
        ProfissionalSaude medico = profissionais.save(new ClinicoGeral("Dr. Historico", "CRM-MG 800004"));
        consultas.save(new ConsultaRotina(comHistorico, medico, LocalDate.of(2026, 1, 20), LocalTime.of(8, 0),
                "rotina", null));

        assertTrue(pacientes.possuiAtendimentos(comHistorico.getId()));
        assertFalse(pacientes.possuiAtendimentos(semHistorico.getId()));
    }
}
