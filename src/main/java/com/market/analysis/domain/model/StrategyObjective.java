package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Value object representing a strategy's objective for Risk:Reward calculation.
 * Defines entry, target (take profit), and stop loss price levels.
 * This is a deterministic, immutable object used to calculate R:R ratios.
 */
@Getter
@Builder
@ToString
public class StrategyObjective {

    /**
     * Target profit price level (take profit).
     * Must be greater than entry price for long positions.
     */
    private final BigDecimal targetPrice;

    /**
     * Stop loss price level.
     * Must be less than entry price for long positions.
     */
    private final BigDecimal stopLossPrice;

    /**
     * Type of position: LONG or SHORT.
     * Defaults to LONG if not specified.
     */
    private final PositionType positionType;

    /**
     * Optional description of the objective.
     */
    private final String description;

    /**
     * Enum defining position types.
     */
    public enum PositionType {
        LONG, SHORT
    }

    /**
     * Validates the consistency of the objective.
     * Ensures target and stop loss are properly configured relative to position type.
     *
     * @param entryPrice current price at entry (from Stock)
     * @throws IllegalStateException if the objective is not properly configured
     */
    public void validateConsistency(BigDecimal entryPrice) {
        if (targetPrice == null) {
            throw new IllegalStateException("Target price cannot be null");
        }
        if (stopLossPrice == null) {
            throw new IllegalStateException("Stop loss price cannot be null");
        }
        if (entryPrice == null) {
            throw new IllegalStateException("Entry price cannot be null");
        }
        if (positionType == null) {
            throw new IllegalStateException("Position type cannot be null");
        }

        // Validate that target and stop loss are different (check this first)
        if (targetPrice.compareTo(stopLossPrice) == 0) {
            throw new IllegalStateException("Target price and stop loss price cannot be the same");
        }

        // Validate price relationships based on position type
        if (positionType == PositionType.LONG) {
            if (targetPrice.compareTo(entryPrice) <= 0) {
                throw new IllegalStateException(
                        String.format("For LONG positions, target price (%.2f) must be greater than entry price (%.2f)",
                                targetPrice.doubleValue(), entryPrice.doubleValue()));
            }
            if (stopLossPrice.compareTo(entryPrice) >= 0) {
                throw new IllegalStateException(
                        String.format("For LONG positions, stop loss price (%.2f) must be less than entry price (%.2f)",
                                stopLossPrice.doubleValue(), entryPrice.doubleValue()));
            }
        } else if (positionType == PositionType.SHORT) {
            if (targetPrice.compareTo(entryPrice) >= 0) {
                throw new IllegalStateException(
                        String.format("For SHORT positions, target price (%.2f) must be less than entry price (%.2f)",
                                targetPrice.doubleValue(), entryPrice.doubleValue()));
            }
            if (stopLossPrice.compareTo(entryPrice) <= 0) {
                throw new IllegalStateException(
                        String.format("For SHORT positions, stop loss price (%.2f) must be greater than entry price (%.2f)",
                                stopLossPrice.doubleValue(), entryPrice.doubleValue()));
            }
        }
    }

    /**
     * Calculates the Risk:Reward ratio based on entry price.
     * R:R = Potential Reward / Potential Risk
     * 
     * For LONG: Reward = (Target - Entry), Risk = (Entry - StopLoss)
     * For SHORT: Reward = (Entry - Target), Risk = (StopLoss - Entry)
     *
     * @param entryPrice current price at entry (from Stock)
     * @return Risk:Reward ratio as BigDecimal
     * @throws IllegalArgumentException if calculation is invalid
     */
    public BigDecimal calculateRiskRewardRatio(BigDecimal entryPrice) {
        validateConsistency(entryPrice);

        BigDecimal reward;
        BigDecimal risk;

        if (positionType == PositionType.LONG) {
            reward = targetPrice.subtract(entryPrice);
            risk = entryPrice.subtract(stopLossPrice);
        } else {
            reward = entryPrice.subtract(targetPrice);
            risk = stopLossPrice.subtract(entryPrice);
        }

        // Ensure risk is not zero to avoid division by zero
        if (risk.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Risk cannot be zero");
        }

        // Calculate R:R ratio with 2 decimal places
        return reward.divide(risk, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculates potential reward percentage based on entry price.
     * 
     * @param entryPrice current price at entry
     * @return reward percentage (e.g., 10.5 for 10.5%)
     */
    public BigDecimal calculateRewardPercentage(BigDecimal entryPrice) {
        if (entryPrice == null || entryPrice.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Entry price must be greater than zero");
        }

        BigDecimal reward;
        if (positionType == PositionType.LONG) {
            reward = targetPrice.subtract(entryPrice);
        } else {
            reward = entryPrice.subtract(targetPrice);
        }

        return reward.divide(entryPrice, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculates potential risk percentage based on entry price.
     * 
     * @param entryPrice current price at entry
     * @return risk percentage (e.g., 5.0 for 5.0%)
     */
    public BigDecimal calculateRiskPercentage(BigDecimal entryPrice) {
        if (entryPrice == null || entryPrice.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Entry price must be greater than zero");
        }

        BigDecimal risk;
        if (positionType == PositionType.LONG) {
            risk = entryPrice.subtract(stopLossPrice);
        } else {
            risk = stopLossPrice.subtract(entryPrice);
        }

        return risk.divide(entryPrice, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StrategyObjective that = (StrategyObjective) o;
        return Objects.equals(targetPrice, that.targetPrice) &&
                Objects.equals(stopLossPrice, that.stopLossPrice) &&
                positionType == that.positionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetPrice, stopLossPrice, positionType);
    }
}
