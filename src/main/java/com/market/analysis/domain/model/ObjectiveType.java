package com.market.analysis.domain.model;

/**
 * Enum representing the types of objectives for strategy targets and stop-loss levels.
 * Used to define how target and stop-loss values are calculated in a trading strategy.
 */
public enum ObjectiveType {
    
    /**
     * Simple Moving Average - based on SMA calculation.
     */
    SMA,
    
    /**
     * Percentage - based on percentage change.
     */
    PERCENTAGE,
    
    /**
     * Fixed Price - based on absolute price value.
     */
    FIXED_PRICE
}
