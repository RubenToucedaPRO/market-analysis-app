package com.market.analysis.domain.exception;

/**
 * Exception thrown when risk/reward calculations result in invalid or unsafe conditions.
 * This is a domain-level exception that represents a business rule violation for risk management.
 */
public class InvalidRiskRewardException extends RuntimeException {

    /**
     * Constructs a new InvalidRiskRewardException with the specified detail message.
     * 
     * @param message the detail message describing the invalid risk/reward condition
     */
    public InvalidRiskRewardException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidRiskRewardException with the specified detail message and cause.
     * 
     * @param message the detail message describing the invalid risk/reward condition
     * @param cause   the underlying cause of the exception
     */
    public InvalidRiskRewardException(String message, Throwable cause) {
        super(message, cause);
    }
}
