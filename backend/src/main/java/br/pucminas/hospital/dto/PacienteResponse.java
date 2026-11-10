package br.pucminas.hospital.dto;

import java.time.LocalDate;

import br.pucminas.hospital.model.Cpf;
import br.pucminas.hospital.model.Paciente;

/** O CPF volta com mascara (000.000.000-00), que e como a tela exibe; no banco ele fica so com digitos. */
public record PacienteResponse(Long id, String nome, String cpf, LocalDate dataNascimento, Integer idade,
        String telefone, String email, EnderecoResponse endereco) {

    public static PacienteResponse de(Paciente p) {
        return new PacienteResponse(p.getId(), p.getNome(), Cpf.formatar(p.getCpf()), p.getDataNascimento(),
                p.getIdade(), p.getTelefone(), p.getEmail(), EnderecoResponse.de(p.getEndereco()));
    }
}
