package com.market.analysis.application.usecase;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * Internal deterministic evaluation outcome for a candidate ticker.
 */
@Builder
public class DeterministicTickerEvaluation {

    @Getter
    private final boolean suitable;
    private final List<String> traceability;

    /**
     * Defensive copy to avoid exposing mutable internals from evaluator implementations.
     */
    public List<String> getTraceability() {
        return traceability != null ? List.copyOf(traceability) : List.of();
    }
}
