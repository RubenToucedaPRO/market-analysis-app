package com.market.analysis.domain.model;

import java.util.Map;
import java.util.Objects;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Entity representing a technical analysis rule.
 * Rules are deterministic and self-contained, evaluating ticker data
 * according to specific technical criteria.
 */
@Getter
@Builder
@ToString
public class Rule {

    /**
     * Unique identifier for the rule.
     */
    private final Long id;

    /**
     * Name of the rule (e.g., "SMA Crossover", "Volume Spike").
     */
    private final String name;

    /**
     * Type of the rule (e.g., "MOVING_AVERAGE", "VOLUME", "PATTERN").
     */
    private final String ruleType;

    /**
     * Parameters for the rule evaluation.
     * Can include thresholds, periods, indicators, etc.
     */
    private final Map<String, Object> parameters;

    /**
     * Description of what the rule evaluates.
     */
    private final String description;

    /**
     * Evaluates the rule against the provided ticker data.
     * This method should remain deterministic - same input always produces same output.
     *
     * @param data the ticker data to evaluate
     * @return RuleResult containing the evaluation result and justification
     * @throws IllegalArgumentException if data is null
     */
    public RuleResult evaluate(TickerData data) {
        if (data == null) {
            throw new IllegalArgumentException("TickerData cannot be null");
        }

        // Validation: ensure required fields are present
        validateRuleConsistency();

        // This is a placeholder implementation
        // Actual evaluation logic would be implemented based on rule type
        // For now, return a basic result
        boolean passed = performEvaluation(data);
        String justification = generateJustification(data, passed);

        return RuleResult.builder()
                .passed(passed)
                .justification(justification)
                .rule(this)
                .build();
    }

    /**
     * Validates that the rule has all required fields and consistency.
     *
     * @throws IllegalStateException if the rule is not properly configured
     */
    private void validateRuleConsistency() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Rule name cannot be null or empty");
        }
        if (ruleType == null || ruleType.trim().isEmpty()) {
            throw new IllegalStateException("Rule type cannot be null or empty");
        }
        if (parameters == null) {
            throw new IllegalStateException("Rule parameters cannot be null");
        }
    }

    /**
     * Performs the actual evaluation logic based on rule type.
     * This is a placeholder that should be implemented with specific rule logic.
     *
     * @param data the ticker data
     * @return true if the rule passes, false otherwise
     */
    private boolean performEvaluation(TickerData data) {
        // Placeholder implementation
        // Real implementation would use Strategy pattern based on ruleType
        return data.getCurrentPrice() != null;
    }

    /**
     * Generates a justification message for the evaluation result.
     *
     * @param data   the ticker data
     * @param passed whether the rule passed
     * @return justification message
     */
    private String generateJustification(TickerData data, boolean passed) {
        if (passed) {
            return String.format("Rule '%s' passed for ticker %s", name, data.getTicker());
        } else {
            return String.format("Rule '%s' failed for ticker %s", name, data.getTicker());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return Objects.equals(id, rule.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
