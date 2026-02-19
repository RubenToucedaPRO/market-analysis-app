package com.market.analysis.domain.model;

/**
 * Enum representing the different types of objectives for calculating target and stop-loss prices.
 */
public enum ObjectiveType {
    /**
     * Simple Moving Average - uses SMA values from the stock (sma20, sma50, sma200).
     */
    SMA,
    
    /**
     * Percentage-based objective - calculates price by adding/subtracting a percentage from entry price.
     */
    PERCENTAGE,
    
    /**
     * Fixed price objective - uses a specific price value directly.
     */
    FIXED_PRICE
}
