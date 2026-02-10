package com.market.analysis.infrastructure.exception;

/**
 * Exception thrown when a RuleDefinition is not found.
 * This is a domain-level exception that represents a business rule violation.
 */
public class StockException extends RuntimeException {

    /**
     * Constructs a new RuleDefinitionNotFoundException with the specified detail
     * message.
     * 
     * @param message the detail message
     */
    public StockException(String message) {
        super(message);
    }

    /**
     * Constructs a new RuleDefinitionNotFoundException with the specified detail
     * message and cause.
     * 
     * @param message the detail message
     * @param cause   the cause
     */
    public StockException(String message, Throwable cause) {
        super(message, cause);
    }
}
