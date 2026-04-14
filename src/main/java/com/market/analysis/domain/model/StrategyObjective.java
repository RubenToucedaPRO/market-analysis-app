package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Value Object representing the objectives and risk management parameters for a trading strategy.
 * Defines target levels, stop-loss levels, and capital risk parameters.
 * This is an immutable, self-contained domain object without infrastructure dependencies.
 */
@Getter
@Builder
@ToString
public class StrategyObjective {

    /**
     * Set of allowed SMA periods for target and stop-loss values.
     * Only SMA20, SMA50, and SMA200 are supported in the system.
     */
    private static final Set<Integer> ALLOWED_SMA_PERIODS = Set.of(20, 50, 200);

    /**
     * Type of calculation method for the target level (e.g., SMA, PERCENTAGE, FIXED_PRICE).
     */
    private final ObjectiveType targetType;

    /**
     * Type of calculation method for the stop-loss level (e.g., SMA, PERCENTAGE, FIXED_PRICE).
     */
    private final ObjectiveType stopLossType;

    /**
     * Target value for the strategy objective.
     * Must be greater than zero.
     * For PERCENTAGE type: represents percentage (e.g., 5.0 for 5%)
     * For FIXED_PRICE type: represents absolute price
     * For SMA type: represents period for moving average calculation
     */
    private final BigDecimal targetValue;

    /**
     * Stop-loss value for risk management.
     * Must be greater than zero.
     * For PERCENTAGE type: represents percentage (e.g., 2.0 for 2%)
     * For FIXED_PRICE type: represents absolute price
     * For SMA type: represents period for moving average calculation
     */
    private final BigDecimal stopLossValue;

    /**
     * Amount of capital to risk in this strategy, expressed as a decimal.
     * Must be greater than zero.
     * Example: 0.02 represents 2% of capital
     */
    private final BigDecimal capitalToRisk;

    /**
     * Human-readable description of the strategy objective.
     * Provides context about the strategy's goals and parameters.
     */
    private final String description;

    /**
     * Validates the consistency and correctness of this strategy objective.
     * Ensures all mandatory fields are present and numeric values are positive.
     *
     * @throws IllegalArgumentException if any validation rule is violated
     */
    public void validate() {
        if (targetType == null) {
            throw new IllegalArgumentException("targetType cannot be null");
        }
        if (stopLossType == null) {
            throw new IllegalArgumentException("stopLossType cannot be null");
        }
        if (targetValue == null) {
            throw new IllegalArgumentException("targetValue cannot be null");
        }
        if (stopLossValue == null) {
            throw new IllegalArgumentException("stopLossValue cannot be null");
        }
        if (capitalToRisk == null) {
            throw new IllegalArgumentException("capitalToRisk cannot be null");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description cannot be null or blank");
        }
        
        if (targetValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("targetValue must be greater than zero");
        }
        if (stopLossValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("stopLossValue must be greater than zero");
        }
        if (capitalToRisk.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("capitalToRisk must be greater than zero");
        }

        validateSmaValue(targetType, targetValue, "targetValue");
        validateSmaValue(stopLossType, stopLossValue, "stopLossValue");
    }

    /**
     * Validates that the given value is an allowed SMA period when the type is SMA.
     * Allowed periods are: 20, 50, and 200.
     *
     * @param type      the objective type
     * @param value     the value to validate
     * @param fieldName the field name for error messages
     * @throws IllegalArgumentException if the type is SMA and the value is not 20, 50, or 200
     */
    private void validateSmaValue(ObjectiveType type, BigDecimal value, String fieldName) {
        if (type == ObjectiveType.SMA && !ALLOWED_SMA_PERIODS.contains(value.intValue())) {
            throw new IllegalArgumentException(
                    String.format("%s must be one of %s when type is SMA, but was %s",
                            fieldName, ALLOWED_SMA_PERIODS, value));
        }
    }
}
