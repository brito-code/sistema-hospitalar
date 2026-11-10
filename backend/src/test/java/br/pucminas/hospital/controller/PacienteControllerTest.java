package br.pucminas.hospital.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import br.pucminas.hospital.GeradorDeCpf;
import br.pucminas.hospital.model.ClinicoGeral;
import br.pucminas.hospital.model.ConsultaRotina;
import br.pucminas.hospital.model.Cpf;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.repository.ConsultaRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;

@SpringBootTest
@AutoConfigureMockMvc
class PacienteControllerTest {

    private static final LocalDate NASCIMENTO = LocalDate.of(1988, 3, 14);

    @Autowired MockMvc mvc;
    @Autowired PacienteRepository pacienteRepo;
    @Autowired ProfissionalSaudeRepository profissionalRepo;
    @Autowired ConsultaRepository consultaRepo;

    private String corpo(String nome, String cpf) {
        return """
                {"nome":"%s","cpf":"%s","dataNascimento":"1988-03-14","telefone":"(31) 98812-3401",
                 "email":"ana.mendes@email.com",
                 "endereco":{"logradouro":"Rua da Bahia","numero":"1200","complemento":"Apto 301","bairro":"Centro",
                             "cidade":"Belo Horizonte","uf":"mg","cep":"30160011"}}""".formatted(nome, cpf);
    }

    private ResultActions cadastrar(String json) throws Exception {
        return mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long id(ResultActions acao) throws Exception {
        String resp = acao.andReturn().getResponse().getContentAsString();
        return Long.parseLong(resp.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void cadastraPacienteEDevolveOsDadosNormalizados() throws Exception {
        String cpf = GeradorDeCpf.proximo();
        int idadeEsperada = Period.between(NASCIMENTO, LocalDate.now()).getYears();

        cadastrar(corpo("Ana Clara Mendes", Cpf.formatar(cpf)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Ana Clara Mendes"))
                .andExpect(jsonPath("$.cpf").value(Cpf.formatar(cpf)))
                .andExpect(jsonPath("$.dataNascimento").value("1988-03-14"))
                .andExpect(jsonPath("$.idade").value(idadeEsperada))
                .andExpect(jsonPath("$.endereco.uf").value("MG"))
                .andExpect(jsonPath("$.endereco.cep").value("30160-011"))
                .andExpect(jsonPath("$.endereco.complemento").value("Apto 301"));
    }

    @Test
    void aceitaCpfSemMascaraEDevolveComMascara() throws Exception {
        String cpf = GeradorDeCpf.proximo();

        cadastrar(corpo("Bruno Costa", cpf))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value(Cpf.formatar(cpf)));
    }

    @Test
    void cpfJaCadastradoRetorna409MesmoComMascaraDiferente() throws Exception {
        String cpf = GeradorDeCpf.proximo();
        cadastrar(corpo("Primeiro", cpf)).andExpect(status().isCreated());

        cadastrar(corpo("Segundo", Cpf.formatar(cpf)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value("Ja existe paciente com o CPF " + Cpf.formatar(cpf)));
    }

    @Test
    void cpfInvalidoRetorna400ApontandoOCampo() throws Exception {
        cadastrar(corpo("Ana", "111.111.111-11"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados invalidos"))
                .andExpect(jsonPath("$.detalhes[0]").value("cpf: CPF invalido"));
    }

    @Test
    void camposObrigatoriosAusentesRetornam400() throws Exception {
        mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes.length()").value(6))
                .andExpect(jsonPath("$.detalhes", hasItem(startsWith("nome"))))
                .andExpect(jsonPath("$.detalhes", hasItem(startsWith("cpf"))))
                .andExpect(jsonPath("$.detalhes", hasItem(startsWith("dataNascimento"))))
                .andExpect(jsonPath("$.detalhes", hasItem(startsWith("telefone"))))
                .andExpect(jsonPath("$.detalhes", hasItem(startsWith("email"))))
                .andExpect(jsonPath("$.detalhes", hasItem(startsWith("endereco"))));
    }

    @Test
    void enderecoInvalidoApontaOCampoAninhado() throws Exception {
        String json = corpo("Ana", GeradorDeCpf.proximo())
                .replace("\"uf\":\"mg\"", "\"uf\":\"XX\"")
                .replace("\"cep\":\"30160011\"", "\"cep\":\"123\"");

        cadastrar(json)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", hasItem(startsWith("endereco.uf"))))
                .andExpect(jsonPath("$.detalhes", hasItem(startsWith("endereco.cep"))));
    }

    @Test
    void dataDeNascimentoNoFuturoRetorna400() throws Exception {
        String json = corpo("Ana", GeradorDeCpf.proximo()).replace("1988-03-14", "2999-01-01");

        cadastrar(json)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", hasItem(startsWith("dataNascimento"))));
    }

    @Test
    void dataForaDoFormatoRetorna400IndicandoOCampo() throws Exception {
        String json = corpo("Ana", GeradorDeCpf.proximo()).replace("1988-03-14", "14/03/1988");

        cadastrar(json)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Requisicao invalida"))
                .andExpect(jsonPath("$.detalhes[0]").value("dataNascimento: valor invalido"));
    }

    @Test
    void buscaPorIdEDevolve404QuandoNaoExiste() throws Exception {
        String cpf = GeradorDeCpf.proximo();
        long id = id(cadastrar(corpo("Camila Souza", cpf)).andExpect(status().isCreated()));

        mvc.perform(get("/pacientes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Camila Souza"));

        mvc.perform(get("/pacientes/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Paciente nao encontrado(a): id 999999"))
                .andExpect(jsonPath("$.caminho").value("/pacientes/999999"));
    }

    @Test
    void listaEmOrdemAlfabeticaEFiltraPorNomeSemDistinguirMaiusculas() throws Exception {
        String marca = GeradorDeCpf.proximo();
        cadastrar(corpo("Beatriz Lima " + marca, GeradorDeCpf.proximo())).andExpect(status().isCreated());
        cadastrar(corpo("Amanda Lima " + marca, GeradorDeCpf.proximo())).andExpect(status().isCreated());

        mvc.perform(get("/pacientes").param("nome", marca))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Amanda Lima " + marca))
                .andExpect(jsonPath("$[1].nome").value("Beatriz Lima " + marca));

        mvc.perform(get("/pacientes").param("nome", "BEATRIZ LIMA " + marca))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Beatriz Lima " + marca));

        mvc.perform(get("/pacientes")).andExpect(status().isOk());
    }

    @Test
    void listagemAguentaPacienteCriadoSoComONome() throws Exception {
        String marca = GeradorDeCpf.proximo();
        pacienteRepo.save(new Paciente("Sem dados " + marca));

        mvc.perform(get("/pacientes").param("nome", marca))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cpf").doesNotExist())
                .andExpect(jsonPath("$[0].idade").doesNotExist())
                .andExpect(jsonPath("$[0].endereco").doesNotExist());
    }

    @Test
    void atualizaDadosMantendoOuTrocandoOCpf() throws Exception {
        String cpf = GeradorDeCpf.proximo();
        long id = id(cadastrar(corpo("Carla Souza", cpf)).andExpect(status().isCreated()));

        // mesmo CPF do proprio paciente nao conta como duplicado
        mvc.perform(put("/pacientes/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Carla Souza Ribeiro", cpf)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Carla Souza Ribeiro"));

        String cpfNovo = GeradorDeCpf.proximo();
        mvc.perform(put("/pacientes/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Carla Souza Ribeiro", cpfNovo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(Cpf.formatar(cpfNovo)));

        mvc.perform(get("/pacientes/" + id))
                .andExpect(jsonPath("$.cpf").value(Cpf.formatar(cpfNovo)));
    }

    @Test
    void atualizacaoRecusaCpfDeOutroPacienteEIdInexistente() throws Exception {
        long id = id(cadastrar(corpo("Diego Alves", GeradorDeCpf.proximo())).andExpect(status().isCreated()));
        String cpfDoOutro = GeradorDeCpf.proximo();
        cadastrar(corpo("Elisa Prado", cpfDoOutro)).andExpect(status().isCreated());

        mvc.perform(put("/pacientes/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Diego Alves", cpfDoOutro)))
                .andExpect(status().isConflict());

        mvc.perform(put("/pacientes/999999").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Ninguem", GeradorDeCpf.proximo())))
                .andExpect(status().isNotFound());

        mvc.perform(put("/pacientes/" + id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removePacienteSemAtendimentos() throws Exception {
        long id = id(cadastrar(corpo("Felipe Duarte", GeradorDeCpf.proximo())).andExpect(status().isCreated()));

        mvc.perform(delete("/pacientes/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/pacientes/" + id)).andExpect(status().isNotFound());
        mvc.perform(delete("/pacientes/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void pacienteComHistoricoDeAtendimentosNaoPodeSerExcluido() throws Exception {
        long id = id(cadastrar(corpo("Gabriela Nunes", GeradorDeCpf.proximo())).andExpect(status().isCreated()));
        Paciente paciente = pacienteRepo.findById(id).orElseThrow();
        ProfissionalSaude medico = profissionalRepo.save(new ClinicoGeral("Dr. Historico", "CRM-MG 800001"));
        consultaRepo.save(new ConsultaRotina(paciente, medico, LocalDate.of(2026, 5, 4), LocalTime.of(9, 0),
                "retorno", null));

        mvc.perform(delete("/pacientes/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem")
                        .value("Paciente possui atendimentos registrados e nao pode ser excluido"));
        mvc.perform(get("/pacientes/" + id)).andExpect(status().isOk());
    }
}
