package com.market.analysis.domain.exception;

/**
 * Exception thrown when an entity cannot be deleted because it has associated dependencies.
 * This is a domain-level exception representing a business rule violation related to integrity.
 */
public class EntityInUseException extends RuntimeException {

    /**
     * Constructs a new EntityInUseException with the specified detail message.
     *
     * @param message the detail message
     */
    public EntityInUseException(String message) {
        super(message);
    }

    /**
     * Constructs a new EntityInUseException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public EntityInUseException(String message, Throwable cause) {
        super(message, cause);
    }
}
