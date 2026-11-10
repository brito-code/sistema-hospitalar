package br.pucminas.hospital.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * O CPF precisa ter 11 digitos (com ou sem mascara) e digitos verificadores corretos.
 * Nulo e vazio passam: quem exige o campo e o @NotBlank.
 */
@Documented
@Constraint(validatedBy = CpfValidador.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfValido {

    String message() default "CPF invalido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
