package br.pucminas.hospital.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.pucminas.hospital.dto.AltaRequest;
import br.pucminas.hospital.dto.InternacaoRequest;
import br.pucminas.hospital.dto.InternacaoResponse;
import br.pucminas.hospital.dto.QuartoRequest;
import br.pucminas.hospital.dto.QuartoResponse;
import br.pucminas.hospital.exception.AltaInvalidaException;
import br.pucminas.hospital.exception.QuartoSemVagaException;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.model.enums.SituacaoQuarto;
import br.pucminas.hospital.model.enums.TipoQuarto;
import br.pucminas.hospital.repository.InternacaoRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;
import br.pucminas.hospital.repository.QuartoRepository;

@SpringBootTest
class InternacaoServiceTest {

    @Autowired InternacaoService internacaoService;
    @Autowired QuartoService quartoService;
    @Autowired InternacaoRepository internacaoRepo;
    @Autowired QuartoRepository quartoRepo;
    @Autowired PacienteRepository pacienteRepo;
    @Autowired ProfissionalSaudeRepository profissionalRepo;

    Long pacienteId;
    Long profissionalId;
    final LocalDate entrada = LocalDate.of(2026, 5, 10);

    @BeforeEach
    void preparar() {
        internacaoRepo.deleteAll();
        quartoRepo.deleteAll();
        pacienteId = pacienteRepo.save(new Paciente("Ana")).getId();
        profissionalId = profissionalRepo.save(new ProfissionalSaude("Dr. X")).getId();
    }

    private Long novoQuarto(int capacidade) {
        return quartoService.criar(new QuartoRequest("1" + capacidade, 1, capacidade, TipoQuarto.ENFERMARIA)).id();
    }

    private InternacaoResponse internar(Long quartoId) {
        return internacaoService.internar(new InternacaoRequest(pacienteId, profissionalId, quartoId, entrada, null, null));
    }

    @Test
    void quartoLotadoRejeitaNovaInternacao() {
        Long quarto = novoQuarto(1);
        internar(quarto);
        assertEquals(SituacaoQuarto.OCUPADO, quartoService.buscar(quarto).situacao());
        assertThrows(QuartoSemVagaException.class, () -> internar(quarto));
    }

    @Test
    void altaLiberaAVagaDoQuarto() {
        Long quarto = novoQuarto(1);
        InternacaoResponse i = internar(quarto);
        InternacaoResponse comAlta = internacaoService.darAlta(i.id(), new AltaRequest(entrada.plusDays(2)));
        assertFalse(comAlta.ativa());
        QuartoResponse q = quartoService.buscar(quarto);
        assertEquals(0, q.ocupacaoAtual());
        assertEquals(SituacaoQuarto.DISPONIVEL, q.situacao());
        internar(quarto); // vaga reaproveitada
    }

    @Test
    void altaDuplicadaEAltaAntesDaEntradaSaoRejeitadas() {
        Long quarto = novoQuarto(2);
        InternacaoResponse i = internar(quarto);
        assertThrows(AltaInvalidaException.class,
                () -> internacaoService.darAlta(i.id(), new AltaRequest(entrada.minusDays(1))));
        internacaoService.darAlta(i.id(), new AltaRequest(entrada));
        assertThrows(AltaInvalidaException.class, () -> internacaoService.darAlta(i.id(), null));
    }

    @Test
    void recursosInexistentesGeramNaoEncontrado() {
        Long quarto = novoQuarto(1);
        assertThrows(RecursoNaoEncontradoException.class, () -> internacaoService.buscar(9999L));
        assertThrows(RecursoNaoEncontradoException.class,
                () -> internacaoService.internar(new InternacaoRequest(9999L, profissionalId, quarto, entrada, null, null)));
        assertThrows(RecursoNaoEncontradoException.class,
                () -> internacaoService.internar(new InternacaoRequest(pacienteId, profissionalId, 9999L, entrada, null, null)));
    }

    @Test
    void naoReduzCapacidadeAbaixoDaOcupacao() {
        Long quarto = novoQuarto(2);
        internar(quarto);
        internar(quarto);
        assertThrows(br.pucminas.hospital.exception.RegraNegocioException.class,
                () -> quartoService.atualizar(quarto, new QuartoRequest("X", 1, 1, TipoQuarto.UTI)));
    }

    @Test
    void filtraQuartosPorSituacao() {
        Long cheio = novoQuarto(1);
        novoQuarto(3);
        internar(cheio);
        assertEquals(1, quartoService.listar(SituacaoQuarto.OCUPADO).size());
        assertEquals(1, quartoService.listar(SituacaoQuarto.DISPONIVEL).size());
        assertEquals(2, quartoService.listar(null).size());
    }
}
