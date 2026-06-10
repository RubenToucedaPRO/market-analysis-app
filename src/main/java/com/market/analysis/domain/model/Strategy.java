package com.market.analysis.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.market.analysis.domain.exception.DomainValidationException;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Entity representing a trading strategy composed of multiple technical
 * analysis rules.
 * Strategies define the criteria for evaluating trading opportunities.
 */
@Getter
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
    private final List<Rule> rules;

    /**
     * Risk management objectives for this strategy.
     * Defines target, stop-loss and capital risk parameters.
     */
    private final StrategyObjective objective;

    @Builder
    public Strategy(Long id, String name, String description, List<Rule> rules, StrategyObjective objective) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.rules = rules == null ? new ArrayList<>() : new ArrayList<>(rules);
        this.objective = objective;
    }

    /**
     * Gets an immutable copy of the rules list to prevent external modification.
     *
     * @return unmodifiable list of rules
     */
    public List<Rule> getRules() {
        return List.copyOf(rules);
    }

    /**
     * Validates the consistency of the strategy.
     * Ensures all required fields are present and valid.
     *
     * @throws IllegalStateException if the strategy is not properly configured
     */
    public void validateConsistency() {
        if (name == null || name.trim().isEmpty()) {
            throw new DomainValidationException("validation.strategy_name_null");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new DomainValidationException("validation.strategy_desc_null");
        }
        if (rules == null || rules.isEmpty()) {
            throw new DomainValidationException("validation.strategy_no_rules");
        }

        // Validate each rule
        for (Rule rule : rules) {
            if (rule == null) {
                throw new DomainValidationException("validation.strategy_null_rule");
            }
            rule.validate();
        }

        if (objective == null) {
            throw new DomainValidationException("validation.strategy_objective_null");
        }
        objective.validate();
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
