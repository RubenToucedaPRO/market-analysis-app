package com.market.analysis.domain.model;

import java.math.BigDecimal;

/**
 * Validation rule that checks if the stock's current price is greater than
 * its SMA 200 (Simple Moving Average 200).
 * This is a common bullish indicator.
 */
public class PriceAboveSma200Rule implements ValidationRule {

    private static final String RULE_ID = "PRICE_ABOVE_SMA200";
    private static final String RULE_NAME = "Price Above SMA200";
    private static final String DESCRIPTION = "Validates that current price is above SMA 200";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }

    @Override
    public boolean evaluate(Stock stock) {
        if (stock == null) {
            return false;
        }

        BigDecimal currentPrice = stock.getCurrentPrice();
        BigDecimal sma200 = stock.getSma200();

        if (currentPrice == null || sma200 == null) {
            return false;
        }

        return currentPrice.compareTo(sma200) > 0;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
