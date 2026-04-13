package com.market.analysis.application.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for RuleDefinition.
 * Used to transfer rule definition data between the presentation layer and views.
 *
 * <p>The {@code allowedParams} and {@code anyParamAllowed} fields are populated
 * from the canonical {@code RuleCapabilityCatalog} at read time so that the UI
 * can render parameter selects with only the values the evaluator actually
 * accepts (P2 – UI guided by capabilities).</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleDefinitionDTO {

    private Long id;
    private String code;
    private String name;
    private boolean requiresParam;
    private String description;

    /**
     * The specific parameter values allowed for this indicator.
     * Populated from the catalog at read time; empty for no-param or any-param indicators.
     */
    private Set<Double> allowedParams;

    /**
     * True when the indicator accepts any numeric parameter value (e.g. CONSTANT, VALUE).
     * Populated from the catalog at read time.
     */
    private boolean anyParamAllowed;
}
