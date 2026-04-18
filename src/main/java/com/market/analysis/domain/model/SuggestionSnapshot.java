package com.market.analysis.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SuggestionSnapshot {

    private final Long strategyId;
    private final Instant suggestedAt;
    private final String appliedFilters;
    private final List<String> unmappableRules;
    private final List<String> warnings;
    private final List<SuggestedTickerSnapshot> suggestedTickers;

    @Builder
    public SuggestionSnapshot(Long strategyId, Instant suggestedAt, String appliedFilters, List<String> unmappableRules,
            List<String> warnings, List<SuggestedTickerSnapshot> suggestedTickers) {
        this.strategyId = strategyId;
        this.suggestedAt = suggestedAt;
        this.appliedFilters = appliedFilters;
        this.unmappableRules = unmappableRules == null ? new ArrayList<>() : new ArrayList<>(unmappableRules);
        this.warnings = warnings == null ? new ArrayList<>() : new ArrayList<>(warnings);
        this.suggestedTickers = suggestedTickers == null ? new ArrayList<>() : new ArrayList<>(suggestedTickers);
    }

    public List<String> getUnmappableRules() {
        return List.copyOf(unmappableRules);
    }

    public List<String> getWarnings() {
        return List.copyOf(warnings);
    }

    public List<SuggestedTickerSnapshot> getSuggestedTickers() {
        return List.copyOf(suggestedTickers);
    }
}
