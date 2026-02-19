package com.market.analysis.domain.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Value object representing a strategy objective for calculating target or stop-loss prices.
 * Contains the type of objective and the associated value.
 */
@Getter
@Builder
@ToString
public class StrategyObjective {
    
    /**
     * The type of objective (SMA, PERCENTAGE, or FIXED_PRICE).
     */
    private final ObjectiveType type;
    
    /**
     * The value associated with the objective.
     * - For SMA: the period (20, 50, or 200)
     * - For PERCENTAGE: the percentage value (e.g., 5.0 for 5%)
     * - For FIXED_PRICE: the target price value
     */
    private final BigDecimal value;
    
    /**
     * Validates that the objective has valid data.
     * 
     * @throws IllegalArgumentException if type or value is null
     */
    public void validate() {
        if (type == null) {
            throw new IllegalArgumentException("ObjectiveType cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Objective value cannot be null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Objective value cannot be negative");
        }
    }
}
