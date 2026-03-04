package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.market.analysis.domain.exception.MissingIndicatorException;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyObjective;

/**
 * Domain service responsible for calculating risk-reward metrics for trading
 * strategies.
 * Performs deterministic calculations for target prices, stop-loss levels, and
 * position sizing.
 * 
 * This is a pure domain service with no infrastructure dependencies,
 * ensuring deterministic and testable calculation logic following financial
 * best practices.
 */
public class RiskRewardCalculator {

    private static final int PRICE_SCALE = 4;
    private static final int RATIO_SCALE = 4;
    private static final RoundingMode PRICE_ROUNDING = RoundingMode.HALF_UP;
    private static final RoundingMode POSITION_ROUNDING = RoundingMode.DOWN;
    private static final String ENTRY_PRICE_NULL_MSG = "Entry price cannot be null";
    private static final String ENTRY_PRICE = "Entry price";

    /**
     * Calculates the target price for a strategy based on the objective type.
     * 
     * @param entryPrice the entry price for the position
     * @param objective  the strategy objective containing target configuration
     * @param stock      the stock data containing technical indicators
     * @return the calculated target price
     * @throws IllegalArgumentException  if parameters are null or invalid
     * @throws MissingIndicatorException if required SMA indicator is missing
     */
    public BigDecimal calculateTargetPrice(BigDecimal entryPrice, StrategyObjective objective, Stock stock) {
        Objects.requireNonNull(entryPrice, ENTRY_PRICE_NULL_MSG);
        Objects.requireNonNull(objective, "Strategy objective cannot be null");
        Objects.requireNonNull(stock, "Stock cannot be null");

        validatePositivePrice(entryPrice, ENTRY_PRICE);

        return switch (objective.getTargetType()) {
            case SMA -> resolveSmaValue(objective.getTargetValue(), stock, "target");
            case PERCENTAGE -> calculatePercentagePrice(entryPrice, objective.getTargetValue(), true);
            case FIXED_PRICE -> validateAndReturnFixedPrice(objective.getTargetValue(), "Target");
        };
    }

    /**
     * Calculates the stop-loss price for a strategy based on the objective type.
     * 
     * @param entryPrice the entry price for the position
     * @param objective  the strategy objective containing stop-loss configuration
     * @param stock      the stock data containing technical indicators
     * @return the calculated stop-loss price
     * @throws IllegalArgumentException  if parameters are null, invalid, or if
     *                                   stop-loss >= entry price
     * @throws MissingIndicatorException if required SMA indicator is missing
     */
    public BigDecimal calculateStopLossPrice(BigDecimal entryPrice, StrategyObjective objective, Stock stock) {
        Objects.requireNonNull(entryPrice, ENTRY_PRICE_NULL_MSG);
        Objects.requireNonNull(objective, "Strategy objective cannot be null");
        Objects.requireNonNull(stock, "Stock cannot be null");

        validatePositivePrice(entryPrice, ENTRY_PRICE);

        BigDecimal stopLossPrice = switch (objective.getStopLossType()) {
            case SMA -> resolveSmaValue(objective.getStopLossValue(), stock, "stop-loss");
            case PERCENTAGE -> calculatePercentagePrice(entryPrice, objective.getStopLossValue(), false);
            case FIXED_PRICE -> validateAndReturnFixedPrice(objective.getStopLossValue(), "Stop-loss");
        };

        validateStopLossPrice(entryPrice, stopLossPrice);

        return stopLossPrice;
    }

    /**
     * Calculates the risk-reward ratio for a trading position.
     * 
     * @param entryPrice  the entry price for the position
     * @param targetPrice the target price for taking profit
     * @param stopPrice   the stop-loss price for risk management
     * @return the risk-reward ratio (potential reward / potential risk)
     * @throws IllegalArgumentException if parameters are null, invalid, or
     *                                  mathematically inconsistent
     */
    public BigDecimal calculateRiskRewardRatio(BigDecimal entryPrice, BigDecimal targetPrice, BigDecimal stopPrice) {
        Objects.requireNonNull(entryPrice, ENTRY_PRICE_NULL_MSG);
        Objects.requireNonNull(targetPrice, "Target price cannot be null");
        Objects.requireNonNull(stopPrice, "Stop price cannot be null");

        validatePositivePrice(entryPrice, ENTRY_PRICE);
        validatePositivePrice(targetPrice, "Target price");
        validatePositivePrice(stopPrice, "Stop price");

        // For long positions: target should be above entry, stop should be below entry
        if (targetPrice.compareTo(entryPrice) <= 0) {
            throw new IllegalArgumentException("Target price must be greater than entry price for long positions");
        }
        if (stopPrice.compareTo(entryPrice) >= 0) {
            throw new IllegalArgumentException("Stop price must be less than entry price for long positions");
        }

        BigDecimal potentialReward = targetPrice.subtract(entryPrice);
        BigDecimal potentialRisk = entryPrice.subtract(stopPrice);

        if (potentialRisk.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Potential risk must be greater than zero");
        }

        return potentialReward.divide(potentialRisk, RATIO_SCALE, PRICE_ROUNDING);
    }

    /**
     * Calculates the position size (number of shares) based on capital at risk.
     * Position size is rounded DOWN to ensure we never exceed the maximum risk.
     * 
     * @param entryPrice    the entry price for the position
     * @param stopPrice     the stop-loss price
     * @param capitalToRisk the total capital amount to risk on this position
     * @return the number of shares to buy (rounded down)
     * @throws IllegalArgumentException if parameters are null, invalid, or
     *                                  mathematically inconsistent
     */
    public BigDecimal calculatePositionSize(BigDecimal entryPrice, BigDecimal stopPrice, BigDecimal capitalToRisk) {
        Objects.requireNonNull(entryPrice, ENTRY_PRICE_NULL_MSG);
        Objects.requireNonNull(stopPrice, "Stop price cannot be null");
        Objects.requireNonNull(capitalToRisk, "Capital to risk cannot be null");

        validatePositivePrice(entryPrice, ENTRY_PRICE);
        validatePositivePrice(stopPrice, "Stop price");
        validatePositivePrice(capitalToRisk, "Capital to risk");

        if (stopPrice.compareTo(entryPrice) >= 0) {
            throw new IllegalArgumentException("Stop price must be less than entry price for long positions");
        }

        BigDecimal riskPerShare = entryPrice.subtract(stopPrice);

        if (riskPerShare.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Risk per share must be greater than zero");
        }

        // Round DOWN to ensure we never exceed the maximum risk
        return capitalToRisk.divide(riskPerShare, 0, POSITION_ROUNDING);
    }

    /**
     * Resolves the SMA value from stock data based on the period parameter.
     * Only supports periods 20, 50, and 200.
     * 
     * @param periodValue the SMA period (must be 20, 50, or 200)
     * @param stock       the stock data containing SMA values
     * @param context     context description for error messages (e.g., "target",
     *                    "stop-loss")
     * @return the SMA value with proper scaling
     * @throws IllegalArgumentException  if period is not 20, 50, or 200
     * @throws MissingIndicatorException if the required SMA value is null in stock
     *                                   data
     */
    private BigDecimal resolveSmaValue(BigDecimal periodValue, Stock stock, String context) {
        Objects.requireNonNull(periodValue, "SMA period value cannot be null");

        int period = periodValue.intValue();

        BigDecimal smaValue = switch (period) {
            case 20 -> stock.getSma20();
            case 50 -> stock.getSma50();
            case 200 -> stock.getSma200();
            default -> throw new IllegalArgumentException(
                    String.format("SMA period %d is not supported. Only periods 20, 50, and 200 are allowed.", period));
        };

        if (smaValue == null) {
            throw new MissingIndicatorException(
                    String.format("SMA%d value is required for %s calculation but is missing in stock data for %s",
                            period, context, stock.getTicker()));
        }

        return smaValue.setScale(PRICE_SCALE, PRICE_ROUNDING);
    }

    /**
     * Calculates a price based on percentage change from entry price.
     * 
     * @param entryPrice      the base price
     * @param percentageValue the percentage value (e.g., 5.0 for 5%)
     * @param isTarget        true if calculating target (add percentage), false for
     *                        stop-loss (subtract percentage)
     * @return the calculated price
     */
    private BigDecimal calculatePercentagePrice(BigDecimal entryPrice, BigDecimal percentageValue, boolean isTarget) {
        Objects.requireNonNull(percentageValue, "Percentage value cannot be null");

        if (percentageValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Percentage value must be greater than zero");
        }

        BigDecimal multiplier = percentageValue.divide(BigDecimal.valueOf(100), RATIO_SCALE, PRICE_ROUNDING);
        BigDecimal change = entryPrice.multiply(multiplier);

        BigDecimal result = isTarget ? entryPrice.add(change) : entryPrice.subtract(change);
        return result.setScale(PRICE_SCALE, PRICE_ROUNDING);
    }

    /**
     * Validates and returns a fixed price value.
     * 
     * @param fixedPrice the fixed price value
     * @param context    context description for error messages
     * @return the fixed price with proper scaling
     */
    private BigDecimal validateAndReturnFixedPrice(BigDecimal fixedPrice, String context) {
        Objects.requireNonNull(fixedPrice, context + " fixed price cannot be null");
        validatePositivePrice(fixedPrice, context + " fixed price");
        return fixedPrice.setScale(PRICE_SCALE, PRICE_ROUNDING);
    }

    /**
     * Validates that a price value is positive.
     * 
     * @param price     the price to validate
     * @param fieldName the field name for error messages
     * @throws IllegalArgumentException if price is not positive
     */
    private void validatePositivePrice(BigDecimal price, String fieldName) {
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
    }

    /**
     * Validates that stop-loss price is less than entry price for long positions.
     * 
     * @param entryPrice    the entry price
     * @param stopLossPrice the stop-loss price
     * @throws IllegalArgumentException if stop-loss price is >= entry price
     */
    private void validateStopLossPrice(BigDecimal entryPrice, BigDecimal stopLossPrice) {
        if (stopLossPrice.compareTo(entryPrice) >= 0) {
            throw new IllegalArgumentException(
                    String.format("Stop-loss price (%.2f) must be less than entry price (%.2f) for long positions",
                            stopLossPrice.doubleValue(), entryPrice.doubleValue()));
        }
    }
}
