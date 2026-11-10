package br.pucminas.hospital.dto;

import org.hibernate.Hibernate;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import br.pucminas.hospital.model.Cirurgiao;
import br.pucminas.hospital.model.ClinicoGeral;
import br.pucminas.hospital.model.MedicoEspecialista;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.model.enums.TipoProfissional;

/** Saida polimorfica de profissional: o campo {@code tipo} indica quais campos extras vem no JSON. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProfissionalResponse.ClinicoGeralResponse.class, name = TipoProfissional.Valor.CLINICO_GERAL),
        @JsonSubTypes.Type(value = ProfissionalResponse.EspecialistaResponse.class, name = TipoProfissional.Valor.ESPECIALISTA),
        @JsonSubTypes.Type(value = ProfissionalResponse.CirurgiaoResponse.class, name = TipoProfissional.Valor.CIRURGIAO)
})
public sealed interface ProfissionalResponse {

    Long id();
    String nome();
    String registroProfissional();
    String telefone();
    String email();

    record ClinicoGeralResponse(Long id, String nome, String registroProfissional, String telefone, String email)
            implements ProfissionalResponse {
    }

    record EspecialistaResponse(Long id, String nome, String registroProfissional, String telefone, String email,
            Especialidade especialidade) implements ProfissionalResponse {
    }

    record CirurgiaoResponse(Long id, String nome, String registroProfissional, String telefone, String email,
            String areaCirurgica) implements ProfissionalResponse {
    }

    static ProfissionalResponse de(ProfissionalSaude profissional) {
        Object real = Hibernate.unproxy(profissional);
        if (real instanceof MedicoEspecialista m) {
            return new EspecialistaResponse(m.getId(), m.getNome(), m.getRegistroProfissional(), m.getTelefone(),
                    m.getEmail(), m.getEspecialidade());
        }
        if (real instanceof Cirurgiao c) {
            return new CirurgiaoResponse(c.getId(), c.getNome(), c.getRegistroProfissional(), c.getTelefone(),
                    c.getEmail(), c.getAreaCirurgica());
        }
        if (real instanceof ClinicoGeral g) {
            return new ClinicoGeralResponse(g.getId(), g.getNome(), g.getRegistroProfissional(), g.getTelefone(),
                    g.getEmail());
        }
        throw new IllegalArgumentException("Tipo de profissional sem resposta: " + real.getClass().getSimpleName());
    }
}
