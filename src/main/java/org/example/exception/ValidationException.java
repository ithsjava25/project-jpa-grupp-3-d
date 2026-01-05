package org.example.exception;

public class ValidationException extends ApplicationException {
    private final String fieldName;

    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
    }

    public ValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public ValidationException(String fieldName, String message, String errorCode) {
        super(message, errorCode);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
