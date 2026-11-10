package br.pucminas.hospital.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import br.pucminas.hospital.model.ConsultaRotina;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.repository.ConsultaRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ProfissionalControllerTest {

    /** Registros unicos: o banco de teste e compartilhado com as outras classes. */
    private static final AtomicInteger SEQUENCIA = new AtomicInteger(700000);

    @Autowired MockMvc mvc;
    @Autowired PacienteRepository pacienteRepo;
    @Autowired ProfissionalSaudeRepository profissionalRepo;
    @Autowired ConsultaRepository consultaRepo;

    long pacienteId;

    @BeforeEach
    void preparar() {
        pacienteId = pacienteRepo.save(new Paciente("Ana")).getId();
    }

    private static String novoCrm() {
        return "CRM-MG " + SEQUENCIA.incrementAndGet();
    }

    private ResultActions cadastrar(String json) throws Exception {
        return mvc.perform(post("/profissionais").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long id(ResultActions acao) throws Exception {
        String resp = acao.andReturn().getResponse().getContentAsString();
        return Long.parseLong(resp.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private String clinico(String crm) {
        return """
                {"tipo":"CLINICO_GERAL","nome":"Dr. Rafael Nunes","registroProfissional":"%s",
                 "telefone":"(31) 3224-1002","email":"rafael.nunes@hospital.edu.br"}""".formatted(crm);
    }

    private String especialista(String crm, String especialidade) {
        return """
                {"tipo":"ESPECIALISTA","nome":"Dra. Mariana Lopes","registroProfissional":"%s",
                 "especialidade":"%s"}""".formatted(crm, especialidade);
    }

    private String cirurgiao(String crm) {
        return """
                {"tipo":"CIRURGIAO","nome":"Dr. Lucas Andrade","registroProfissional":"%s",
                 "areaCirurgica":"Ortopedica"}""".formatted(crm);
    }

    @Test
    void cadastraCadaSubtipoComJsonPolimorfico() throws Exception {
        ResultActions clinico = cadastrar(clinico(novoCrm()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.tipo").value("CLINICO_GERAL"))
                .andExpect(jsonPath("$.telefone").value("(31) 3224-1002"))
                .andExpect(jsonPath("$.especialidade").doesNotExist());

        cadastrar(especialista(novoCrm(), "CARDIOLOGIA"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ESPECIALISTA"))
                .andExpect(jsonPath("$.especialidade").value("CARDIOLOGIA"));

        cadastrar(cirurgiao(novoCrm()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("CIRURGIAO"))
                .andExpect(jsonPath("$.areaCirurgica").value("Ortopedica"));

        mvc.perform(get("/profissionais/" + id(clinico)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CLINICO_GERAL"))
                .andExpect(jsonPath("$.nome").value("Dr. Rafael Nunes"));
    }

    @Test
    void tipoAusenteOuInvalidoEDadosInvalidosRetornam400() throws Exception {
        cadastrar("""
                {"nome":"Sem tipo","registroProfissional":"CRM-MG 12345"}""")
                .andExpect(status().isBadRequest());
        cadastrar("""
                {"tipo":"ENFERMEIRO","nome":"X","registroProfissional":"CRM-MG 12345"}""")
                .andExpect(status().isBadRequest());
        cadastrar(especialista(novoCrm(), "PEDIATRIA"))
                .andExpect(status().isBadRequest());
        cadastrar("""
                {"tipo":"ESPECIALISTA","nome":"Dra. X","registroProfissional":"%s"}""".formatted(novoCrm()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value(org.hamcrest.Matchers.startsWith("especialidade")));
        cadastrar(clinico("123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registroProfissionalNaoPodeRepetir() throws Exception {
        String crm = novoCrm();
        cadastrar(clinico(crm)).andExpect(status().isCreated());
        cadastrar(cirurgiao(crm)).andExpect(status().isConflict());
    }

    @Test
    void filtraProfissionaisPorEspecialidade() throws Exception {
        long neuro = id(cadastrar(especialista(novoCrm(), "NEUROLOGIA")).andExpect(status().isCreated()));
        cadastrar(especialista(novoCrm(), "ORTOPEDIA")).andExpect(status().isCreated());
        cadastrar(clinico(novoCrm())).andExpect(status().isCreated());

        mvc.perform(get("/profissionais").param("especialidade", "NEUROLOGIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) neuro)))
                .andExpect(jsonPath("$[*].tipo", everyItem(is("ESPECIALISTA"))))
                .andExpect(jsonPath("$[*].especialidade", everyItem(is("NEUROLOGIA"))));

        mvc.perform(get("/profissionais").param("especialidade", "INEXISTENTE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizaDadosMasNaoTrocaOTipo() throws Exception {
        String crm = novoCrm();
        long id = id(cadastrar(especialista(crm, "CARDIOLOGIA")).andExpect(status().isCreated()));

        mvc.perform(put("/profissionais/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(especialista(crm, "NEUROLOGIA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especialidade").value("NEUROLOGIA"));

        mvc.perform(put("/profissionais/" + id).contentType(MediaType.APPLICATION_JSON).content(clinico(crm)))
                .andExpect(status().isConflict());
    }

    @Test
    void clinicoGeralEncaminhaParaEspecialistasDisponiveis() throws Exception {
        long clinicoId = id(cadastrar(clinico(novoCrm())).andExpect(status().isCreated()));
        long ortopedista = id(cadastrar(especialista(novoCrm(), "ORTOPEDIA")).andExpect(status().isCreated()));

        mvc.perform(post("/profissionais/" + clinicoId + "/encaminhamentos").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"pacienteId":%d,"especialidade":"ORTOPEDIA"}""".formatted(pacienteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clinicoGeralId").value(clinicoId))
                .andExpect(jsonPath("$.pacienteId").value(pacienteId))
                .andExpect(jsonPath("$.especialidade").value("ORTOPEDIA"))
                .andExpect(jsonPath("$.especialistasDisponiveis[*].id", hasItem((int) ortopedista)))
                .andExpect(jsonPath("$.especialistasDisponiveis[0].tipo").value("ESPECIALISTA"));
    }

    @Test
    void somenteClinicoGeralEncaminha() throws Exception {
        long cirurgiaoId = id(cadastrar(cirurgiao(novoCrm())).andExpect(status().isCreated()));

        mvc.perform(post("/profissionais/" + cirurgiaoId + "/encaminhamentos").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"pacienteId":%d,"especialidade":"ORTOPEDIA"}""".formatted(pacienteId)))
                .andExpect(status().isConflict());

        long clinicoId = id(cadastrar(clinico(novoCrm())).andExpect(status().isCreated()));
        mvc.perform(post("/profissionais/" + clinicoId + "/encaminhamentos").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"pacienteId":999999,"especialidade":"ORTOPEDIA"}"""))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeProfissionalSemAtendimentosEBloqueiaComAtendimentos() throws Exception {
        long livre = id(cadastrar(clinico(novoCrm())).andExpect(status().isCreated()));
        mvc.perform(delete("/profissionais/" + livre)).andExpect(status().isNoContent());
        mvc.perform(get("/profissionais/" + livre)).andExpect(status().isNotFound());

        long ocupado = id(cadastrar(clinico(novoCrm())).andExpect(status().isCreated()));
        ProfissionalSaude profissional = profissionalRepo.findById(ocupado).orElseThrow();
        Paciente paciente = pacienteRepo.findById(pacienteId).orElseThrow();
        consultaRepo.save(new ConsultaRotina(paciente, profissional, LocalDate.of(2026, 12, 1), LocalTime.of(9, 0),
                "retorno", null));

        mvc.perform(delete("/profissionais/" + ocupado)).andExpect(status().isConflict());
    }
}
