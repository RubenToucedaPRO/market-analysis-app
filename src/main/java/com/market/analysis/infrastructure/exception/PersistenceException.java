package com.market.analysis.infrastructure.exception;

/**
 * Exception thrown when a database or persistence operation fails.
 * This is an infrastructure-level exception that wraps technical exceptions
 * from the persistence layer (SQLException, DataAccessException, etc.).
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
     * This constructor should be used to maintain exception traceability.
     * 
     * @param message the detail message
     * @param cause   the cause (technical exception from persistence layer)
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
