package org.example.service;

import lombok.extern.slf4j.XSlf4j;
import org.example.exception.ValidationException;

import java.util.regex.Pattern;

@XSlf4j
public class ValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$"
    );
    private static final Pattern ORG_NUM_PATTERN = Pattern.compile("^\\d{6}-\\d{4}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\s\\-\\(\\)]{7,20}$");

    public void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("email", "Email cannot be null or empty", "EMAIL_REQUIRED");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("email", "Invalid email format: " + email, "EMAIL_INVALID");
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("password", "Password cannot be null or empty", "PASSWORD_REQUIRED");
        }

        if (password.length() < 8) {
            throw new ValidationException("password", "Password must be at least 8 characters", "PASSWORD_TOO_SHORT");
        }
    }
}
