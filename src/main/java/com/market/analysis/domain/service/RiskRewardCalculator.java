package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

import com.market.analysis.domain.exception.DomainValidationException;
import com.market.analysis.domain.exception.MissingIndicatorException;
import com.market.analysis.domain.model.IndicatorCode;
import com.market.analysis.domain.model.RuleCapabilityCatalog;
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

    private static final String FIELD_ENTRY_PRICE = "Entry price";
    private static final String FIELD_TARGET_PRICE = "Target price";
    private static final String FIELD_STOP_PRICE = "Stop price";
    private static final String FIELD_CAPITAL_TO_RISK = "Capital to risk";

    /**
     * Calculates the target price for a strategy based on the objective type.
     * 
     * @param entryPrice the entry price for the position
     * @param objective  the strategy objective containing target configuration
     * @param stock      the stock data containing technical indicators
     * @return the calculated target price
     * @throws DomainValidationException if parameters are null or invalid
     * @throws MissingIndicatorException if required SMA indicator is missing
     */
    public BigDecimal calculateTargetPrice(BigDecimal entryPrice, StrategyObjective objective, Stock stock) {
        requireNonNull(entryPrice, "validation.entry_price_null");
        requireNonNull(objective, "validation.strategy_objective_null");
        requireNonNull(stock, "validation.stock_null");

        validatePositivePrice(entryPrice, FIELD_ENTRY_PRICE);

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
     * @throws DomainValidationException if parameters are null, invalid, or if
     *                                   stop-loss >= entry price
     * @throws MissingIndicatorException if required SMA indicator is missing
     */
    public BigDecimal calculateStopLossPrice(BigDecimal entryPrice, StrategyObjective objective, Stock stock) {
        requireNonNull(entryPrice, "validation.entry_price_null");
        requireNonNull(objective, "validation.strategy_objective_null");
        requireNonNull(stock, "validation.stock_null");

        validatePositivePrice(entryPrice, FIELD_ENTRY_PRICE);

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
     * @throws DomainValidationException if parameters are null, invalid, or
     *                                  mathematically inconsistent
     */
    public BigDecimal calculateRiskRewardRatio(BigDecimal entryPrice, BigDecimal targetPrice, BigDecimal stopPrice) {
        requireNonNull(entryPrice, "validation.entry_price_null");
        requireNonNull(targetPrice, "validation.target_price_null");
        requireNonNull(stopPrice, "validation.stop_price_null");

        validatePositivePrice(entryPrice, FIELD_ENTRY_PRICE);
        validatePositivePrice(targetPrice, FIELD_TARGET_PRICE);
        validatePositivePrice(stopPrice, FIELD_STOP_PRICE);

        if (targetPrice.compareTo(entryPrice) <= 0) {
            throw new DomainValidationException("validation.target_below_entry");
        }
        if (stopPrice.compareTo(entryPrice) >= 0) {
            throw new DomainValidationException("validation.stop_above_entry");
        }

        BigDecimal potentialReward = targetPrice.subtract(entryPrice);
        BigDecimal potentialRisk = entryPrice.subtract(stopPrice);

        if (potentialRisk.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException("validation.risk_zero");
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
     * @throws DomainValidationException if parameters are null, invalid, or
     *                                  mathematically inconsistent
     */
    public BigDecimal calculatePositionSize(BigDecimal entryPrice, BigDecimal stopPrice, BigDecimal capitalToRisk) {
        requireNonNull(entryPrice, "validation.entry_price_null");
        requireNonNull(stopPrice, "validation.stop_price_null");
        requireNonNull(capitalToRisk, "validation.capital_null");

        validatePositivePrice(entryPrice, FIELD_ENTRY_PRICE);
        validatePositivePrice(stopPrice, FIELD_STOP_PRICE);
        validatePositivePrice(capitalToRisk, FIELD_CAPITAL_TO_RISK);

        if (stopPrice.compareTo(entryPrice) >= 0) {
            throw new DomainValidationException("validation.stop_above_entry");
        }

        BigDecimal riskPerShare = entryPrice.subtract(stopPrice);

        if (riskPerShare.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException("validation.risk_per_share_zero");
        }

        return capitalToRisk.divide(riskPerShare, 0, POSITION_ROUNDING);
    }

    /**
     * Resolves the SMA value from stock data based on the period parameter.
     * Delegates resolution to {@link RuleCapabilityCatalog} as the single source of truth
     * for supported SMA periods and their resolvers.
     * 
     * @param periodValue the SMA period (must be a period supported by the catalog)
     * @param stock       the stock data containing SMA values
     * @param context     context description for error messages (e.g., "target",
     *                    "stop-loss")
     * @return the SMA value with proper scaling
     * @throws DomainValidationException if period is not supported by the catalog
     * @throws MissingIndicatorException if the required SMA value is null in stock
     *                                   data
     */
    private BigDecimal resolveSmaValue(BigDecimal periodValue, Stock stock, String context) {
        requireNonNull(periodValue, "validation.sma_period_null");

        int period = periodValue.intValue();

        BigDecimal smaValue = RuleCapabilityCatalog.getCapability(IndicatorCode.SMA.getCode())
                .map(cap -> cap.resolve((double) period, stock))
                .orElse(null);

        if (smaValue == null) {
            boolean isPeriodSupported = RuleCapabilityCatalog.getCapability(IndicatorCode.SMA.getCode())
                    .map(cap -> cap.isParamAllowed((double) period))
                    .orElse(false);

            if (!isPeriodSupported) {
                throw new DomainValidationException(
                        "validation.sma_period_unsupported", period);
            }

            throw new MissingIndicatorException("rule.missing_indicator");
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
        requireNonNull(percentageValue, "validation.percentage_null");

        if (percentageValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException("validation.percentage_zero");
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
        requireNonNull(fixedPrice, "validation.fixed_price_null");
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
                    String.format(Locale.ENGLISH, "Stop-loss price (%.2f) must be less than entry price (%.2f) for long positions",
                            stopLossPrice.doubleValue(), entryPrice.doubleValue()));
        }
    }

    private static void requireNonNull(Object value, String errorCode) {
        if (value == null) {
            throw new DomainValidationException(errorCode);
        }
    }
}
