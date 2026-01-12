package org.example.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.example.exception.ValidationException;

import java.util.Set;
import java.util.stream.Collectors;

public class ValidationUtil {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> void validate(T dto) {
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(dto);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
            throw new ValidationException(message);
        }
    }
}
