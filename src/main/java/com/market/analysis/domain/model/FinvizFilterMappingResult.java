package com.market.analysis.domain.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Contract result for translating internal rules to Finviz filter expressions.
 */
@Getter
@Builder
@ToString
public class FinvizFilterMappingResult {

    private final String filters;
    private final List<String> unmappableRules;
    private final List<String> warnings;

    public List<String> getUnmappableRules() {
        return unmappableRules != null ? List.copyOf(unmappableRules) : List.of();
    }

    public List<String> getWarnings() {
        return warnings != null ? List.copyOf(warnings) : List.of();
    }

    public boolean hasUnmappableRules() {
        return unmappableRules != null && !unmappableRules.isEmpty();
    }

    public static class FinvizFilterMappingResultBuilder {
        public FinvizFilterMappingResultBuilder unmappableRules(List<String> unmappableRules) {
            this.unmappableRules = unmappableRules != null ? new ArrayList<>(unmappableRules) : new ArrayList<>();
            return this;
        }

        public FinvizFilterMappingResultBuilder warnings(List<String> warnings) {
            this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
            return this;
        }
    }
}
