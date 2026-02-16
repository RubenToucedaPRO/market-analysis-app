package com.market.analysis.domain.exception;

/**
 * Exception thrown when a RuleDefinition is not found.
 * This is a domain-level exception that represents a business rule violation.
 */
public class EvaluateStrategyException extends RuntimeException {

    /**
     * Constructs a new EvaluateStrategyFoundException with the specified detail
     * message.
     * 
     * @param message the detail message
     */
    public EvaluateStrategyException(String message) {
        super(message);
    }

    /**
     * Constructs a new EvaluateStrategyFoundException with the specified detail
     * message and cause.
     * 
     * @param message the detail message
     * @param cause   the cause
     */
    public EvaluateStrategyException(String message, Throwable cause) {
        super(message, cause);
    }
}
