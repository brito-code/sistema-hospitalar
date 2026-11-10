package br.pucminas.hospital.model;

import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.model.enums.TipoProfissional;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

/**
 * Profissional responsavel por atendimentos. A hierarquia fica numa tabela unica,
 * discriminada pela coluna {@code tipo}.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
public abstract class ProfissionalSaude extends Pessoa {

    /** CRM/COREN. A unicidade e garantida pelo servico. */
    private String registroProfissional;

    protected ProfissionalSaude() {
    }

    protected ProfissionalSaude(String nome, String registroProfissional) {
        super(nome);
        this.registroProfissional = registroProfissional;
    }

    /** Por padrao o profissional nao atende especialidade; especialistas sobrescrevem. */
    public boolean atendeEspecialidade(Especialidade especialidade) {
        return false;
    }

    public Especialidade getEspecialidade() {
        return null;
    }

    public abstract TipoProfissional getTipo();

    /** Atualiza os dados comuns a todos os profissionais. */
    public void atualizarDados(String nome, String registroProfissional, String telefone, String email) {
        setNome(nome);
        this.registroProfissional = registroProfissional;
        setTelefone(telefone);
        setEmail(email);
    }

    public String getRegistroProfissional() { return registroProfissional; }
}
