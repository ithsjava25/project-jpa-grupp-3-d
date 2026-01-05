package org.example.exception;

public class AuthenticationException extends ApplicationException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, String errorCode) {
        super(message, errorCode);
    }
}
