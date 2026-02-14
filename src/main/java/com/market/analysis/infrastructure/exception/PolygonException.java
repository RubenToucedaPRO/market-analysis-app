package com.market.analysis.infrastructure.exception;

/**
 * Exception thrown when a RuleDefinition is not found.
 * This is a domain-level exception that represents a business rule violation.
 */
public class PolygonException extends RuntimeException {

    /**
     * Constructs a new RuleDefinitionNotFoundException with the specified detail
     * message.
     * 
     * @param message the detail message
     */
    public PolygonException(String message) {
        super(message);
    }

    /**
     * Constructs a new RuleDefinitionNotFoundException with the specified detail
     * message and cause.
     * 
     * @param message the detail message
     * @param cause   the cause
     */
    public PolygonException(String message, Throwable cause) {
        super(message, cause);
    }
}
