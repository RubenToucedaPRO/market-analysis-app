package com.market.analysis.domain.exception;

/**
 * Exception thrown when a RuleDefinition is not found.
 * This is a domain-level exception that represents a business rule violation.
 */
public class TickerDataNotFoundException extends RuntimeException {

    /**
     * Constructs a new RuleDefinitionNotFoundException with the specified detail
     * message.
     * 
     * @param message the detail message
     */
    public TickerDataNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new RuleDefinitionNotFoundException with the specified detail
     * message and cause.
     * 
     * @param message the detail message
     * @param cause   the cause
     */
    public TickerDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
