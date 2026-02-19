package com.market.analysis.domain.exception;

/**
 * Exception thrown when a required technical indicator is missing from Stock data.
 * This is a domain-level exception that represents a business rule violation
 * when attempting to calculate risk/reward metrics with incomplete data.
 */
public class MissingIndicatorException extends RuntimeException {

    /**
     * Constructs a new MissingIndicatorException with the specified detail message.
     * 
     * @param message the detail message
     */
    public MissingIndicatorException(String message) {
        super(message);
    }

    /**
     * Constructs a new MissingIndicatorException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause   the cause
     */
    public MissingIndicatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
