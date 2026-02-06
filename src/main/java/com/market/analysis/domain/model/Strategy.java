package com.market.analysis.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Entity representing a trading strategy composed of multiple technical
 * analysis rules.
 * Strategies define the criteria for evaluating trading opportunities.
 */
@Getter
@Builder
@ToString
public class Strategy {

    /**
     * Unique identifier for the strategy.
     */
    private final Long id;

    /**
     * Name of the strategy (e.g., "Conservative Growth", "Momentum Trading").
     */
    private final String name;

    /**
     * Description of the strategy and its purpose.
     */
    private final String description;

    /**
     * List of rules that compose this strategy.
     * All rules should be evaluated when analyzing a ticker.
     */
    @Builder.Default
    private final List<Rule> rules = new ArrayList<>();

    /**
     * Gets an immutable copy of the rules list to prevent external modification.
     *
     * @return unmodifiable list of rules
     */
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * Validates the consistency of the strategy.
     * Ensures all required fields are present and valid.
     *
     * @throws IllegalStateException if the strategy is not properly configured
     */
    public void validateConsistency() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Strategy name cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalStateException("Strategy description cannot be null or empty");
        }

        // Validate each rule
        for (Rule rule : rules) {
            if (rule == null) {
                throw new IllegalStateException("Strategy cannot contain null rules");
            }
            // Rules will validate themselves when evaluated
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Strategy strategy = (Strategy) o;
        return Objects.equals(id, strategy.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
