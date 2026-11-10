package br.pucminas.hospital.model;

import java.util.ArrayList;
import java.util.List;

import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.enums.TipoProfissional;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

/** Medico que realiza procedimentos cirurgicos na sua area. */
@Entity
@DiscriminatorValue(TipoProfissional.Valor.CIRURGIAO)
public class Cirurgiao extends Medico {

    private String areaCirurgica;

    // TODO(dono: Procedimento): mapear como @OneToMany quando ProcedimentoCirurgico virar entidade.
    @Transient
    private List<ProcedimentoCirurgico> procedimentos = new ArrayList<>();

    protected Cirurgiao() {
    }

    public Cirurgiao(String nome, String crm, String areaCirurgica) {
        super(nome, crm);
        this.areaCirurgica = areaCirurgica;
    }

    public void registrarProcedimento(ProcedimentoCirurgico procedimento) {
        if (procedimento == null) {
            throw new RegraNegocioException("Procedimento e obrigatorio");
        }
        procedimentos.add(procedimento);
    }

    public List<ProcedimentoCirurgico> getProcedimentos() { return List.copyOf(procedimentos); }

    public String getAreaCirurgica() { return areaCirurgica; }

    public void setAreaCirurgica(String areaCirurgica) { this.areaCirurgica = areaCirurgica; }

    @Override
    public TipoProfissional getTipo() {
        return TipoProfissional.CIRURGIAO;
    }
}
