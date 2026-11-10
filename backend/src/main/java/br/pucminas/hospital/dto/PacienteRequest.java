package br.pucminas.hospital.dto;

import java.time.LocalDate;

import br.pucminas.hospital.validation.CpfValido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cadastro e atualizacao de paciente. O CPF pode vir com ou sem mascara. */
public record PacienteRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @CpfValido String cpf,
        @NotNull @PastOrPresent LocalDate dataNascimento,
        @NotBlank @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$",
                message = "deve ter DDD e numero, ex.: (31) 99999-9999") String telefone,
        @NotBlank @Email @Size(max = 120) String email,
        @NotNull @Valid EnderecoRequest endereco) {
}
