package com.market.analysis.application.job;

/**
 * Thrown when a background job cannot be accepted because the executor queue
 * is full. The presentation layer maps it to a "busy, retry later" warning
 * instead of blocking the HTTP request thread.
 */
public class JobRejectedException extends RuntimeException {

    public JobRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
