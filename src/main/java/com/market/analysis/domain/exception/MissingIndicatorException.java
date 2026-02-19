package com.market.analysis.domain.exception;

/**
 * Exception thrown when a required technical indicator is missing from the stock data.
 * This is a domain-level exception that represents a business rule violation.
 */
public class MissingIndicatorException extends RuntimeException {

    /**
     * Constructs a new MissingIndicatorException with the specified detail message.
     * 
     * @param message the detail message describing the missing indicator
     */
    public MissingIndicatorException(String message) {
        super(message);
    }

    /**
     * Constructs a new MissingIndicatorException with the specified detail message and cause.
     * 
     * @param message the detail message describing the missing indicator
     * @param cause   the underlying cause of the exception
     */
    public MissingIndicatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
