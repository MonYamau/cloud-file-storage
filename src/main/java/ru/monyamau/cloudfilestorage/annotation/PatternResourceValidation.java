package ru.monyamau.cloudfilestorage.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Pattern(regexp = "^(?:/|(?:[a-zA-Zа-яА-ЯёЁ0-9_ ()\\[\\]{}.,+=\\\\—~!@#$%^&;'-]+/)*[a-zA-Zа-яА-ЯёЁ0-9_ ()\\[\\]{}.,+=\\\\—~!@#$%^&;'-]*)$")
public @interface PatternResourceValidation {
    String message() default "Путь к ресурсу может содержать только латинские и кириллические буквы, пробел, " +
            "цифры и некоторые спецсимволы";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
