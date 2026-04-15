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

        validateSmaperiod(targetType, targetValue, "targetValue");
        validateSmaperiod(stopLossType, stopLossValue, "stopLossValue");
    }

    /**
     * Validates that when the objective type is SMA, the value corresponds to
     * a valid SMA period as defined in {@link RuleCapabilityCatalog}.
     *
     * @param type      the objective type
     * @param value     the numeric value to validate as an SMA period
     * @param fieldName the field name for error messages
     * @throws IllegalArgumentException if the SMA period is not supported
     */
    private void validateSmaperiod(ObjectiveType type, BigDecimal value, String fieldName) {
        if (type != ObjectiveType.SMA) {
            return;
        }
        Set<Double> allowedPeriods = RuleCapabilityCatalog.getCapability("SMA")
                .map(RuleCapability::getAllowedParams)
                .orElse(Set.of());
        double period = value.doubleValue();
        if (!allowedPeriods.contains(period)) {
            throw new IllegalArgumentException(
                    String.format("%s %s is not a valid SMA period. Allowed periods: %s",
                            fieldName, value.stripTrailingZeros().toPlainString(), allowedPeriods));
        }
    }
}
