package br.pucminas.hospital.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Dados de identificacao e contato comuns a pacientes e profissionais. Nao vira tabela propria
 * (@MappedSuperclass): as colunas aparecem em cada tabela filha. O que e especifico de cada papel
 * fica na subclasse (CPF e endereco em {@link Paciente}, registro profissional em {@link ProfissionalSaude}).
 */
@MappedSuperclass
public abstract class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 20)
    private String telefone;

    @Column(length = 120)
    private String email;

    protected Pessoa() {
    }

    protected Pessoa(String nome) {
        this.nome = nome;
    }

    protected Pessoa(String nome, String telefone, String email) {
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
