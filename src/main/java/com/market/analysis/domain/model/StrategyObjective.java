package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Value object representing a strategy's objective for Risk:Reward calculation.
 * Defines target (take profit) and stop loss levels using flexible criteria.
 * This is a deterministic, immutable object used to calculate R:R ratios and position sizing.
 */
@Getter
@Builder
@ToString
public class StrategyObjective {

    /**
     * Type of target definition (SMA, PERCENTAGE, or FIXED_PRICE).
     */
    private final ObjectiveType targetType;

    /**
     * Value for target based on targetType:
     * - SMA: period (e.g., 50 for SMA50)
     * - PERCENTAGE: profit percentage (e.g., 10.0 for 10%)
     * - FIXED_PRICE: absolute price value
     */
    private final BigDecimal targetValue;

    /**
     * Type of stop loss definition (SMA, PERCENTAGE, or FIXED_PRICE).
     */
    private final ObjectiveType stopLossType;

    /**
     * Value for stop loss based on stopLossType:
     * - SMA: period (e.g., 20 for SMA20)
     * - PERCENTAGE: loss percentage (e.g., 5.0 for 5%)
     * - FIXED_PRICE: absolute price value
     */
    private final BigDecimal stopLossValue;

    /**
     * Capital to risk per trade (in currency units).
     * Used to calculate position size based on stop loss distance.
     */
    private final BigDecimal capitalToRisk;

    /**
     * Optional description of the objective.
     */
    private final String description;

    /**
     * Enum defining how target/stop loss are specified.
     */
    public enum ObjectiveType {
        SMA,           // Simple Moving Average period
        PERCENTAGE,    // Percentage from entry price
        FIXED_PRICE    // Absolute price value
    }

    /**
     * Validates the consistency of the objective.
     * Ensures target and stop loss are properly configured.
     *
     * @throws IllegalStateException if the objective is not properly configured
     */
    public void validateConsistency() {
        if (targetType == null) {
            throw new IllegalStateException("Target type cannot be null");
        }
        if (targetValue == null || targetValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Target value must be positive");
        }
        if (stopLossType == null) {
            throw new IllegalStateException("Stop loss type cannot be null");
        }
        if (stopLossValue == null || stopLossValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Stop loss value must be positive");
        }
        if (capitalToRisk != null && capitalToRisk.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Capital to risk must be positive");
        }
    }

    /**
     * Resolves the target price based on the objective type.
     *
     * @param entryPrice current price at entry
     * @param stock stock data containing SMAs if needed
     * @return resolved target price
     */
    public BigDecimal resolveTargetPrice(BigDecimal entryPrice, Stock stock) {
        validateConsistency();
        
        return switch (targetType) {
            case FIXED_PRICE -> targetValue;
            case PERCENTAGE -> entryPrice.add(
                entryPrice.multiply(targetValue).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP)
            );
            case SMA -> resolveSmaValue(targetValue, stock);
        };
    }

    /**
     * Resolves the stop loss price based on the objective type.
     *
     * @param entryPrice current price at entry
     * @param stock stock data containing SMAs if needed
     * @return resolved stop loss price
     */
    public BigDecimal resolveStopLossPrice(BigDecimal entryPrice, Stock stock) {
        validateConsistency();
        
        return switch (stopLossType) {
            case FIXED_PRICE -> stopLossValue;
            case PERCENTAGE -> entryPrice.subtract(
                entryPrice.multiply(stopLossValue).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP)
            );
            case SMA -> resolveSmaValue(stopLossValue, stock);
        };
    }

    /**
     * Resolves SMA value from stock data based on period.
     *
     * @param period SMA period (20, 50, 200, etc.)
     * @param stock stock data
     * @return SMA value
     */
    private BigDecimal resolveSmaValue(BigDecimal period, Stock stock) {
        int periodInt = period.intValue();
        
        return switch (periodInt) {
            case 20 -> stock.getSma20();
            case 50 -> stock.getSma50();
            case 200 -> stock.getSma200();
            default -> throw new IllegalArgumentException(
                String.format("SMA period %d not supported. Supported periods: 20, 50, 200", periodInt)
            );
        };
    }

    /**
     * Calculates the Risk:Reward ratio based on entry price and stock data.
     * R:R = Potential Reward / Potential Risk
     * Reward = (Target - Entry), Risk = (Entry - StopLoss)
     *
     * @param entryPrice current price at entry (from Stock)
     * @param stock stock data for resolving SMAs if needed
     * @return Risk:Reward ratio as BigDecimal
     * @throws IllegalArgumentException if calculation is invalid
     */
    public BigDecimal calculateRiskRewardRatio(BigDecimal entryPrice, Stock stock) {
        BigDecimal targetPrice = resolveTargetPrice(entryPrice, stock);
        BigDecimal stopLossPrice = resolveStopLossPrice(entryPrice, stock);

        // Validate resolved prices
        if (targetPrice.compareTo(entryPrice) <= 0) {
            throw new IllegalArgumentException(
                String.format("Resolved target price (%.2f) must be greater than entry price (%.2f)",
                    targetPrice.doubleValue(), entryPrice.doubleValue())
            );
        }
        if (stopLossPrice.compareTo(entryPrice) >= 0) {
            throw new IllegalArgumentException(
                String.format("Resolved stop loss price (%.2f) must be less than entry price (%.2f)",
                    stopLossPrice.doubleValue(), entryPrice.doubleValue())
            );
        }

        BigDecimal reward = targetPrice.subtract(entryPrice);
        BigDecimal risk = entryPrice.subtract(stopLossPrice);

        if (risk.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Risk cannot be zero");
        }

        return reward.divide(risk, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculates potential reward percentage based on entry price and stock data.
     * 
     * @param entryPrice current price at entry
     * @param stock stock data for resolving SMAs if needed
     * @return reward percentage (e.g., 10.5 for 10.5%)
     */
    public BigDecimal calculateRewardPercentage(BigDecimal entryPrice, Stock stock) {
        if (entryPrice == null || entryPrice.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Entry price must be greater than zero");
        }

        BigDecimal targetPrice = resolveTargetPrice(entryPrice, stock);
        BigDecimal reward = targetPrice.subtract(entryPrice);

        return reward.divide(entryPrice, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculates potential risk percentage based on entry price and stock data.
     * 
     * @param entryPrice current price at entry
     * @param stock stock data for resolving SMAs if needed
     * @return risk percentage (e.g., 5.0 for 5.0%)
     */
    public BigDecimal calculateRiskPercentage(BigDecimal entryPrice, Stock stock) {
        if (entryPrice == null || entryPrice.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Entry price must be greater than zero");
        }

        BigDecimal stopLossPrice = resolveStopLossPrice(entryPrice, stock);
        BigDecimal risk = entryPrice.subtract(stopLossPrice);

        return risk.divide(entryPrice, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculates the number of shares to buy based on capital to risk.
     * Shares = Capital to Risk / (Entry Price - Stop Loss Price)
     * 
     * @param entryPrice current price at entry
     * @param stock stock data for resolving SMAs if needed
     * @return number of shares to purchase (rounded down to whole shares)
     */
    public Integer calculateShareQuantity(BigDecimal entryPrice, Stock stock) {
        if (capitalToRisk == null) {
            return null; // No position sizing if capital to risk not specified
        }

        BigDecimal stopLossPrice = resolveStopLossPrice(entryPrice, stock);
        BigDecimal riskPerShare = entryPrice.subtract(stopLossPrice);

        if (riskPerShare.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Risk per share must be positive");
        }

        // Calculate shares and round down to whole number
        BigDecimal shares = capitalToRisk.divide(riskPerShare, 0, java.math.RoundingMode.DOWN);
        return shares.intValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StrategyObjective that = (StrategyObjective) o;
        return targetType == that.targetType &&
                Objects.equals(targetValue, that.targetValue) &&
                stopLossType == that.stopLossType &&
                Objects.equals(stopLossValue, that.stopLossValue) &&
                Objects.equals(capitalToRisk, that.capitalToRisk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetType, targetValue, stopLossType, stopLossValue, capitalToRisk);
    }
}
