package org.example.exception;

public class EntityNotFoundException extends ApplicationException {
    private final String entityName;
    private final Object identifier;

    public EntityNotFoundException(String entityName, Object identifier) {
        super(String.format("%s not found with identifier: %s", entityName, identifier));
        this.entityName = entityName;
        this.identifier = identifier;
    }

    public EntityNotFoundException(String entityName, Object identifier, String errorCode) {
        super(String.format("%s not found with identifier: %s", entityName, identifier), errorCode);
        this.entityName = entityName;
        this.identifier = identifier;
    }

    public String getEntityName() {
        return entityName;
    }

    public Object getIdentifier() {
        return identifier;
    }
}
