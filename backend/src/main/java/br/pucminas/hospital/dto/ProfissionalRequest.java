package br.pucminas.hospital.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.model.enums.TipoProfissional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Entrada polimorfica de profissional. O campo {@code tipo} escolhe o subtipo:
 * {@code CLINICO_GERAL}, {@code ESPECIALISTA} ou {@code CIRURGIAO}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProfissionalRequest.ClinicoGeralRequest.class, name = TipoProfissional.Valor.CLINICO_GERAL),
        @JsonSubTypes.Type(value = ProfissionalRequest.EspecialistaRequest.class, name = TipoProfissional.Valor.ESPECIALISTA),
        @JsonSubTypes.Type(value = ProfissionalRequest.CirurgiaoRequest.class, name = TipoProfissional.Valor.CIRURGIAO)
})
public sealed interface ProfissionalRequest {

    String FORMATO_CRM = "^CRM-[A-Z]{2} \\d{4,7}$";
    String MENSAGEM_CRM = "deve seguir o formato CRM-UF 00000";

    String nome();
    String registroProfissional();
    String telefone();
    String email();
    TipoProfissional tipo();

    record ClinicoGeralRequest(
            @NotBlank String nome,
            @NotBlank @Pattern(regexp = FORMATO_CRM, message = MENSAGEM_CRM) String registroProfissional,
            String telefone,
            @Email String email) implements ProfissionalRequest {

        @Override
        public TipoProfissional tipo() { return TipoProfissional.CLINICO_GERAL; }
    }

    record EspecialistaRequest(
            @NotBlank String nome,
            @NotBlank @Pattern(regexp = FORMATO_CRM, message = MENSAGEM_CRM) String registroProfissional,
            String telefone,
            @Email String email,
            @NotNull Especialidade especialidade) implements ProfissionalRequest {

        @Override
        public TipoProfissional tipo() { return TipoProfissional.ESPECIALISTA; }
    }

    record CirurgiaoRequest(
            @NotBlank String nome,
            @NotBlank @Pattern(regexp = FORMATO_CRM, message = MENSAGEM_CRM) String registroProfissional,
            String telefone,
            @Email String email,
            @NotBlank String areaCirurgica) implements ProfissionalRequest {

        @Override
        public TipoProfissional tipo() { return TipoProfissional.CIRURGIAO; }
    }
}
