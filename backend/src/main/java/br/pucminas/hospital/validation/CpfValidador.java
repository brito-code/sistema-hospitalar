package br.pucminas.hospital.validation;

import br.pucminas.hospital.model.Cpf;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidador implements ConstraintValidator<CpfValido, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        return valor == null || valor.isBlank() || Cpf.valido(valor);
    }
}
