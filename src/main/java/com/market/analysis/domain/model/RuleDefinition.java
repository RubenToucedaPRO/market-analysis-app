package com.market.analysis.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Entity representing the definition of a rule or indicator in the system.
 * Contains information about the rule's code, name, whether it requires
 * parameters, and a description.
 * This is used to define the available rules that can be evaluated against
 * market data.
 */
@Getter
@Builder
@ToString
public class RuleDefinition {

    private final Long id;

    /**
     * Unique code to identify the indicator in the Java code.
     * Examples: "PRICE", "SMA", "VOLUME", "CONSTANT".
     */
    private final String code;

    /**
     * Human-readable name to display in the frontend.
     * Examples: "Current Price", "Simple Moving Average", "Fixed Value".
     */
    private final String name;

    /**
     * Indicates whether this indicator requires a numeric parameter (such as the
     * period 200).
     */
    private final boolean requiresParam;

    /**
     * Description of the indicator and its purpose.
     */
    private final String description;
}
