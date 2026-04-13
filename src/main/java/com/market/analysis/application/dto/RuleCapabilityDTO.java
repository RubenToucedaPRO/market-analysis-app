package com.market.analysis.application.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a catalog capability entry for presentation.
 * Carries the runtime constraints of a supported rule indicator so that
 * the UI can filter selects and parameter fields to only show valid combinations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleCapabilityDTO {

    /** Canonical indicator code (uppercase, e.g. "SMA", "PRICE"). */
    private String code;

    /** Whether this indicator requires a numeric parameter. */
    private boolean requiresParam;

    /** True when any numeric value is accepted as a parameter (e.g. CONSTANT). */
    private boolean anyParamAllowed;

    /**
     * The specific parameter values allowed by this indicator.
     * Empty when the indicator accepts any value or no value at all.
     */
    private Set<Double> allowedParams;
}
