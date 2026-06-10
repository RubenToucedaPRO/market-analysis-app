package com.market.analysis.domain.model;

/**
 * Canonical enumeration of strategy evaluation outcomes.
 *
 * <p>Used across the domain and application layers to represent
 * the pass/fail result of a rule or strategy evaluation in a type-safe manner.</p>
 */
public enum EvaluationStatus {

    PASSED("PASSED"),
    FAILED("FAILED");

    private final String status;

    EvaluationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
