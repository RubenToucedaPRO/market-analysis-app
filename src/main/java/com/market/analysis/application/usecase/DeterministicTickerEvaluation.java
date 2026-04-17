package com.market.analysis.application.usecase;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * Internal deterministic evaluation outcome for a candidate ticker.
 */
@Getter
@Builder
public class DeterministicTickerEvaluation {

    private final boolean suitable;
    private final List<String> traceability;

    public List<String> getTraceability() {
        return traceability != null ? List.copyOf(traceability) : List.of();
    }
}
