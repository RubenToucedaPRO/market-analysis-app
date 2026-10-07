package com.market.analysis.application.job;

/**
 * Thrown when a suggestion job cannot be accepted because the background
 * executor queue is full. The presentation layer maps it to a "busy, retry
 * later" warning instead of blocking the HTTP request thread.
 */
public class SuggestJobRejectedException extends RuntimeException {

    public SuggestJobRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
