package com.market.analysis.infrastructure.exception;

/**
 * Exception thrown when a database operation fails.
 * This is an infrastructure-level exception that wraps persistence errors.
 */
public class PersistenceException extends RuntimeException {

    /**
     * Constructs a new PersistenceException with the specified detail message.
     * 
     * @param message the detail message
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * Constructs a new PersistenceException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
