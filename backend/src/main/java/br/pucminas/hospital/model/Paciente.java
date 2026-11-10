package br.pucminas.hospital.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import br.pucminas.hospital.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

/**
 * Pessoa atendida pelo hospital. Um paciente acumula consultas e internacoes ao longo do tempo:
 * a chave estrangeira fica em {@link Atendimento}, entao aqui a lista e so o lado inverso da relacao.
 */
@Entity
public class Paciente extends Pessoa {

    /** Somente os 11 digitos. A unicidade e conferida no servico e garantida de novo pelo banco. */
    @Column(unique = true, length = 11)
    private String cpf;

    private LocalDate dataNascimento;

    @Embedded
    private Endereco endereco;

    @OneToMany(mappedBy = "paciente")
    private List<Atendimento> atendimentos = new ArrayList<>();

    protected Paciente() {
    }

    /** Atalho para montar paciente em testes das outras fatias. O cadastro pela API usa o construtor completo. */
    public Paciente(String nome) {
        super(nome);
    }

    public Paciente(String nome, String cpf, LocalDate dataNascimento, String telefone, String email,
            Endereco endereco) {
        super(nome == null ? null : nome.trim(), telefone == null ? null : telefone.trim(), normalizarEmail(email));
        definirDadosCadastrais(cpf, dataNascimento, endereco);
    }

    /** Atualiza todos os dados cadastrais, aplicando as mesmas regras do cadastro. */
    public void atualizarDados(String nome, String cpf, LocalDate dataNascimento, String telefone, String email,
            Endereco endereco) {
        definirDadosCadastrais(cpf, dataNascimento, endereco);
        setNome(nome == null ? null : nome.trim());
        setTelefone(telefone == null ? null : telefone.trim());
        setEmail(normalizarEmail(email));
    }

    private void definirDadosCadastrais(String cpf, LocalDate dataNascimento, Endereco endereco) {
        if (!Cpf.valido(cpf)) {
            throw new RegraNegocioException("CPF invalido");
        }
        if (dataNascimento == null || dataNascimento.isAfter(LocalDate.now())) {
            throw new RegraNegocioException("Data de nascimento invalida");
        }
        this.cpf = Cpf.normalizar(cpf);
        this.dataNascimento = dataNascimento;
        this.endereco = endereco;
    }

    private static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Idade em anos completos hoje. Nula se a data de nascimento nao foi registrada. */
    public Integer getIdade() {
        return calcularIdade(LocalDate.now());
    }

    /** Idade em anos completos na data informada (separada de getIdade para dar para testar sem depender do relogio). */
    public Integer calcularIdade(LocalDate referencia) {
        if (dataNascimento == null) {
            return null;
        }
        return Period.between(dataNascimento, referencia).getYears();
    }

    /** Historico completo (consultas e internacoes), do atendimento mais antigo para o mais recente. */
    public List<Atendimento> getAtendimentos() {
        return atendimentos.stream()
                .sorted(Comparator.comparing(Atendimento::getDataReferencia,
                        Comparator.nullsLast(Comparator.<LocalDateTime>naturalOrder())))
                .toList();
    }

    public List<Consulta> getConsultas() {
        return atendimentos.stream()
                .filter(Consulta.class::isInstance)
                .map(Consulta.class::cast)
                .toList();
    }

    public List<Internacao> getInternacoes() {
        return atendimentos.stream()
                .filter(Internacao.class::isInstance)
                .map(Internacao.class::cast)
                .toList();
    }

    public String getCpf() { return cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public Endereco getEndereco() { return endereco; }
}
