package com.market.analysis.infrastructure.exception;

/**
 * Exception thrown when an AI service operation fails.
 * This is an infrastructure-level exception for AI service integration errors.
 */
public class AIServiceException extends RuntimeException {

    /**
     * Constructs a new AIServiceException with the specified detail message.
     * 
     * @param message the detail message
     */
    public AIServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new AIServiceException with the specified detail message and cause.
     * This constructor should be used to maintain exception traceability.
     * 
     * @param message the detail message
     * @param cause   the cause (technical exception from AI service)
     */
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
