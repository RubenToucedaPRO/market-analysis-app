package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.market.analysis.domain.exception.InvalidRiskRewardException;
import com.market.analysis.domain.exception.MissingIndicatorException;
import com.market.analysis.domain.model.ObjectiveType;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyObjective;

/**
 * Domain service responsible for calculating risk/reward metrics including
 * target prices, stop-loss prices, position sizing, and risk/reward ratios.
 * 
 * This is a pure domain service with no infrastructure dependencies,
 * ensuring deterministic and testable calculation logic.
 */
public class RiskRewardCalculator {
    
    private static final int PRICE_SCALE = 2;
    private static final int RATIO_SCALE = 2;
    
    /**
     * Calculates the target price based on entry price, strategy objective, and stock data.
     * 
     * @param entryPrice the entry price for the trade
     * @param objective the strategy objective defining how to calculate the target
     * @param stock the stock data containing technical indicators
     * @return the calculated target price
     * @throws IllegalArgumentException if any parameter is null
     * @throws MissingIndicatorException if required SMA data is not available
     */
    public BigDecimal calculateTargetPrice(BigDecimal entryPrice, StrategyObjective objective, Stock stock) {
        Objects.requireNonNull(entryPrice, "Entry price cannot be null");
        Objects.requireNonNull(objective, "Strategy objective cannot be null");
        Objects.requireNonNull(stock, "Stock cannot be null");
        
        objective.validate();
        
        return switch (objective.getType()) {
            case SMA -> resolveSmaValue(objective.getValue(), stock);
            case PERCENTAGE -> calculatePercentagePrice(entryPrice, objective.getValue(), true);
            case FIXED_PRICE -> objective.getValue().setScale(PRICE_SCALE, RoundingMode.HALF_UP);
        };
    }
    
    /**
     * Calculates the stop-loss price based on entry price, strategy objective, and stock data.
     * Validates that the stop-loss is less than the entry price for long positions.
     * 
     * @param entryPrice the entry price for the trade
     * @param objective the strategy objective defining how to calculate the stop-loss
     * @param stock the stock data containing technical indicators
     * @return the calculated stop-loss price
     * @throws IllegalArgumentException if any parameter is null
     * @throws MissingIndicatorException if required SMA data is not available
     * @throws InvalidRiskRewardException if stop-loss is >= entry price for a long position
     */
    public BigDecimal calculateStopLossPrice(BigDecimal entryPrice, StrategyObjective objective, Stock stock) {
        Objects.requireNonNull(entryPrice, "Entry price cannot be null");
        Objects.requireNonNull(objective, "Strategy objective cannot be null");
        Objects.requireNonNull(stock, "Stock cannot be null");
        
        objective.validate();
        
        BigDecimal stopPrice = switch (objective.getType()) {
            case SMA -> resolveSmaValue(objective.getValue(), stock);
            case PERCENTAGE -> calculatePercentagePrice(entryPrice, objective.getValue(), false);
            case FIXED_PRICE -> objective.getValue().setScale(PRICE_SCALE, RoundingMode.HALF_UP);
        };
        
        // Security validation: stop-loss must be less than entry price for long positions
        if (stopPrice.compareTo(entryPrice) >= 0) {
            throw new InvalidRiskRewardException(
                String.format("Stop-loss price (%.2f) must be less than entry price (%.2f) for long positions",
                    stopPrice, entryPrice)
            );
        }
        
        return stopPrice;
    }
    
    /**
     * Calculates the risk/reward ratio for a trade.
     * 
     * @param entryPrice the entry price for the trade
     * @param target the target price
     * @param stop the stop-loss price
     * @return the risk/reward ratio
     * @throws IllegalArgumentException if any parameter is null or if stop >= entry
     */
    public BigDecimal calculateRiskRewardRatio(BigDecimal entryPrice, BigDecimal target, BigDecimal stop) {
        Objects.requireNonNull(entryPrice, "Entry price cannot be null");
        Objects.requireNonNull(target, "Target price cannot be null");
        Objects.requireNonNull(stop, "Stop-loss price cannot be null");
        
        if (stop.compareTo(entryPrice) >= 0) {
            throw new IllegalArgumentException(
                "Stop-loss price must be less than entry price for risk/reward calculation"
            );
        }
        
        BigDecimal risk = entryPrice.subtract(stop);
        BigDecimal reward = target.subtract(entryPrice);
        
        if (risk.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Risk cannot be zero");
        }
        
        return reward.divide(risk, RATIO_SCALE, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculates the position size (number of shares) based on the risk parameters.
     * Uses RoundingMode.DOWN to ensure the position never exceeds the maximum risk.
     * 
     * @param entryPrice the entry price for the trade
     * @param stopPrice the stop-loss price
     * @param capitalToRisk the amount of capital willing to risk on this trade
     * @return the number of shares to purchase
     * @throws IllegalArgumentException if any parameter is null or if stop >= entry
     */
    public BigDecimal calculatePositionSize(BigDecimal entryPrice, BigDecimal stopPrice, BigDecimal capitalToRisk) {
        Objects.requireNonNull(entryPrice, "Entry price cannot be null");
        Objects.requireNonNull(stopPrice, "Stop-loss price cannot be null");
        Objects.requireNonNull(capitalToRisk, "Capital to risk cannot be null");
        
        if (stopPrice.compareTo(entryPrice) >= 0) {
            throw new IllegalArgumentException(
                "Stop-loss price must be less than entry price for position sizing"
            );
        }
        
        if (capitalToRisk.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Capital to risk must be positive");
        }
        
        BigDecimal riskPerShare = entryPrice.subtract(stopPrice);
        
        // Use RoundingMode.DOWN to never exceed the maximum risk
        return capitalToRisk.divide(riskPerShare, 0, RoundingMode.DOWN);
    }
    
    /**
     * Resolves the SMA value from the stock data based on the period.
     * Only supports periods 20, 50, and 200.
     * 
     * @param period the SMA period
     * @param stock the stock data
     * @return the SMA value
     * @throws IllegalArgumentException if period is not 20, 50, or 200
     * @throws MissingIndicatorException if the SMA value is null
     */
    private BigDecimal resolveSmaValue(BigDecimal period, Stock stock) {
        int smaValue = period.intValue();
        
        BigDecimal sma = switch (smaValue) {
            case 20 -> stock.getSma20();
            case 50 -> stock.getSma50();
            case 200 -> stock.getSma200();
            default -> throw new IllegalArgumentException(
                String.format("Only SMA periods of 20, 50, and 200 are supported. Got: %d", smaValue)
            );
        };
        
        if (sma == null) {
            throw new MissingIndicatorException(
                String.format("SMA%d is not available for ticker %s", smaValue, stock.getTicker())
            );
        }
        
        return sma.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculates a price based on a percentage adjustment to the entry price.
     * 
     * @param entryPrice the entry price
     * @param percentage the percentage to apply
     * @param isTarget true if calculating target (add percentage), false for stop-loss (subtract percentage)
     * @return the calculated price
     */
    private BigDecimal calculatePercentagePrice(BigDecimal entryPrice, BigDecimal percentage, boolean isTarget) {
        BigDecimal multiplier = percentage.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal adjustment = entryPrice.multiply(multiplier);
        
        BigDecimal result = isTarget 
            ? entryPrice.add(adjustment) 
            : entryPrice.subtract(adjustment);
            
        return result.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }
}
