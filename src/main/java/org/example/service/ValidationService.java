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

    public void validateOrgNum(String orgNum) {
        if (orgNum == null || orgNum.isBlank()) {
            throw new ValidationException("orgNum", "Organization number cannot be null or empty", "ORG_NUM_REQUIRED");
        }

        if (!ORG_NUM_PATTERN.matcher(orgNum).matches()) {
            throw new ValidationException("orgNum", "Invalid organization number format. Expected: 123456-7890", "ORG_NUM_INVALID");
        }
    }

    public void validateCompanyName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("name", "Company name cannot be null or empty", "COMPANY_NAME_REQUIRED");
        }

        if (name.length() < 2) {
            throw new ValidationException("name", "Company name must be at least 2 characters", "COMPANY_NAME_TOO_SHORT");
        }

        if (name.length() > 20) {
            throw new ValidationException("name", "Company name cannot exceed 20 characters", "COMPANY_NAME_TOO_LONG");
        }
    }

    public void validatePersonName(String fieldName, String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException(fieldName, fieldName + " cannot be null or empty", "NAME_REQUIRED");
        }

        if (name.length() < 2) {
            throw new ValidationException(fieldName, fieldName + " must be at least 2 characters", "NAME_TOO_SHORT");
        }

        if (name.length() > 20) {
            throw new ValidationException(fieldName, fieldName + " cannot exceed 20 characters", "NAME_TOO_LONG");
        }

        if (!name.matches("^[a-zA-Z\\s\\-']+$")) {
            throw new ValidationException(fieldName, fieldName + " contains invalid characters", "NAME_INVALID_CHARS");
        }
    }

    public void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return;
        }

        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new ValidationException("phoneNumber", "Invalid phone number format", "PHONE_INVALID");
        }
    }

    public void validateAddress(String fieldName, String address) {
        if (address == null || address.isBlank()) {
            return;
        }

        if (address.length() > 70) {
            throw new ValidationException(fieldName, fieldName + " cannot exceed 70 characters", "ADDRESS_TOO_LONG");
        }
    }

    public void validateInvoiceNumber(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new ValidationException("invoiceNumber", "Invoice number cannot be null or empty", "INVOICE_NUMBER_REQUIRED");
        }

        if (!invoiceNumber.matches("^INV-\\d{4}-\\d{4}$")) {
            throw new ValidationException("invoiceNumber", "Invoice number must be in format INV-YYYY-XXXX", "INVOICE_NUMBER_INVALID");
        }
    }

}
