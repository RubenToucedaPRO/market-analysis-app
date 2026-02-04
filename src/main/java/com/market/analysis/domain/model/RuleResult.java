package com.market.analysis.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Entity representing the result of a rule evaluation.
 * Contains the boolean result and a justification for the decision.
 */
@Getter
@Builder
@ToString
public class RuleResult {

    /**
     * The boolean result of the rule evaluation.
     */
    private final boolean passed;

    /**
     * Justification or explanation for the evaluation result.
     * Provides context about why the rule passed or failed.
     */
    private final String justification;

    /**
     * The rule that was evaluated.
     */
    private final Rule rule;
}
