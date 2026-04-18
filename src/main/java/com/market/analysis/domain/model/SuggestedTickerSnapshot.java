package com.market.analysis.domain.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SuggestedTickerSnapshot {

    private final String ticker;
    private final String suitabilityStatus;
    private final List<String> traceability;

    @Builder
    public SuggestedTickerSnapshot(String ticker, String suitabilityStatus, List<String> traceability) {
        this.ticker = ticker;
        this.suitabilityStatus = suitabilityStatus;
        this.traceability = traceability == null ? new ArrayList<>() : new ArrayList<>(traceability);
    }

    public List<String> getTraceability() {
        return List.copyOf(traceability);
    }
}
