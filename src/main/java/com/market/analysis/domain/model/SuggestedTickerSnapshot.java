package com.market.analysis.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SuggestedTickerSnapshot {

    private final String ticker;
    private final Long strategyId;
    private final Instant suggestedAt;
    private final String suitabilityStatus;
    private final List<String> deterministicMetrics;
    private final List<String> traceability;

    @Builder
    public SuggestedTickerSnapshot(String ticker, Long strategyId, Instant suggestedAt, String suitabilityStatus,
            List<String> deterministicMetrics, List<String> traceability) {
        this.ticker = ticker;
        this.strategyId = strategyId;
        this.suggestedAt = suggestedAt;
        this.suitabilityStatus = suitabilityStatus;
        this.deterministicMetrics =
                deterministicMetrics == null ? new ArrayList<>() : new ArrayList<>(deterministicMetrics);
        this.traceability = traceability == null ? new ArrayList<>() : new ArrayList<>(traceability);
    }

    public List<String> getDeterministicMetrics() {
        return List.copyOf(deterministicMetrics);
    }

    public List<String> getTraceability() {
        return List.copyOf(traceability);
    }
}
