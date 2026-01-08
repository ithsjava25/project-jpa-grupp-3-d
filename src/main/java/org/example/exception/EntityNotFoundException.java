package org.example.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entityName, Object identifier) {
        super(String.format("%s not found with identifier: %s", entityName, identifier));
    }

    public EntityNotFoundException(String entityName, Object identifier, Throwable cause) {
        super(String.format("%s not found with identifier: %s", entityName, identifier), cause);
    }
}
