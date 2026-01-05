package org.example.exception;

public abstract class ApplicationException extends RuntimeException {
    private final String errorCode;

    public ApplicationException(String message) {
        super(message);
        this.errorCode = null;
    }

    public ApplicationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
