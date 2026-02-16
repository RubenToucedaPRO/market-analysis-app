package com.market.analysis.domain.exception;

/**
 * Exception thrown when evaluation of a strategy fails or cannot be completed.
 * This is a domain-level exception that represents a failure during strategy evaluation.
 */
public class EvaluateStrategyException extends RuntimeException {

    /**
     * Constructs a new EvaluateStrategyException with the specified detail
     * message.
     * 
     * @param message the detail message describing the strategy evaluation error
     */
    public EvaluateStrategyException(String message) {
        super(message);
    }

    /**
     * Constructs a new EvaluateStrategyException with the specified detail
     * message and cause.
     * 
     * @param message the detail message describing the strategy evaluation error
     * @param cause   the underlying cause of the evaluation failure
     */
    public EvaluateStrategyException(String message, Throwable cause) {
        super(message, cause);
    }
}
