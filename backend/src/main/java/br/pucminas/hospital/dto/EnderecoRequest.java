package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.Endereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(
        @NotBlank @Size(max = 120) String logradouro,
        @NotBlank @Size(max = 10) String numero,
        @Size(max = 60) String complemento,
        @NotBlank @Size(max = 80) String bairro,
        @NotBlank @Size(max = 80) String cidade,
        @NotBlank @Pattern(regexp = Endereco.PADRAO_UF, flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "deve ser uma UF valida (ex.: MG)") String uf,
        @NotBlank @Pattern(regexp = Endereco.PADRAO_CEP, message = "deve ter 8 digitos (00000-000)") String cep) {

    public Endereco paraEntidade() {
        return new Endereco(logradouro, numero, complemento, bairro, cidade, uf, cep);
    }
}
