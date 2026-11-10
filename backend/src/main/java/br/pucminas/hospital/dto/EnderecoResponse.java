package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.Endereco;

public record EnderecoResponse(String logradouro, String numero, String complemento, String bairro, String cidade,
        String uf, String cep) {

    /** Paciente cadastrado sem endereco (so acontece em fixtures de teste) volta com endereco nulo. */
    public static EnderecoResponse de(Endereco e) {
        if (e == null) {
            return null;
        }
        return new EnderecoResponse(e.getLogradouro(), e.getNumero(), e.getComplemento(), e.getBairro(),
                e.getCidade(), e.getUf(), e.getCepFormatado());
    }
}
