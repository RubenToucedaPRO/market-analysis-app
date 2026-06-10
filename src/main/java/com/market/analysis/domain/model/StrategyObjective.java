package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.util.Set;

import com.market.analysis.domain.exception.DomainErrorCodes;
import com.market.analysis.domain.exception.DomainValidationException;

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
            throw new DomainValidationException(DomainErrorCodes.TARGET_TYPE_NULL);
        }
        if (stopLossType == null) {
            throw new DomainValidationException(DomainErrorCodes.STOP_LOSS_TYPE_NULL);
        }
        if (targetValue == null) {
            throw new DomainValidationException(DomainErrorCodes.TARGET_VALUE_NULL);
        }
        if (stopLossValue == null) {
            throw new DomainValidationException(DomainErrorCodes.STOP_LOSS_VALUE_NULL);
        }
        if (capitalToRisk == null) {
            throw new DomainValidationException(DomainErrorCodes.CAPITAL_TO_RISK_NULL);
        }
        if (description == null || description.isBlank()) {
            throw new DomainValidationException(DomainErrorCodes.DESCRIPTION_NULL);
        }
        
        if (targetValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException(DomainErrorCodes.TARGET_VALUE_ZERO);
        }
        if (stopLossValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException(DomainErrorCodes.STOP_LOSS_VALUE_ZERO);
        }
        if (capitalToRisk.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException(DomainErrorCodes.CAPITAL_TO_RISK_ZERO);
        }

        validateSmaPeriod(targetType, targetValue, "targetValue");
        validateSmaPeriod(stopLossType, stopLossValue, "stopLossValue");
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
    private void validateSmaPeriod(ObjectiveType type, BigDecimal value, String fieldName) {
        if (type != ObjectiveType.SMA) {
            return;
        }
        Set<Double> allowedPeriods = RuleCapabilityCatalog.getCapability(IndicatorCode.SMA.getCode())
                .map(RuleCapability::getAllowedParams)
                .orElse(Set.of());
        double period = value.doubleValue();
        if (!allowedPeriods.contains(period)) {
            throw new DomainValidationException(
                    DomainErrorCodes.SMA_PERIOD_INVALID, fieldName, value.stripTrailingZeros().toPlainString());
        }
    }
}
