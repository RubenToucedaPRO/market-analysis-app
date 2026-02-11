package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.PriceAboveSma200Rule;
import com.market.analysis.domain.model.Stock;

@DisplayName("PriceAboveSma200Rule Tests")
class PriceAboveSma200RuleTest {

    private final PriceAboveSma200Rule rule = new PriceAboveSma200Rule();

    @Test
    @DisplayName("Should return true when current price is above SMA200")
    void shouldReturnTrueWhenPriceIsAboveSma200() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(200.00))
                .sma200(BigDecimal.valueOf(150.00))
                .build();

        assertTrue(rule.evaluate(stock));
    }

    @Test
    @DisplayName("Should return false when current price is below SMA200")
    void shouldReturnFalseWhenPriceIsBelowSma200() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(100.00))
                .sma200(BigDecimal.valueOf(150.00))
                .build();

        assertFalse(rule.evaluate(stock));
    }

    @Test
    @DisplayName("Should return false when current price equals SMA200")
    void shouldReturnFalseWhenPriceEqualsSma200() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .sma200(BigDecimal.valueOf(150.00))
                .build();

        assertFalse(rule.evaluate(stock));
    }

    @Test
    @DisplayName("Should return false when current price is null")
    void shouldReturnFalseWhenCurrentPriceIsNull() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(null)
                .sma200(BigDecimal.valueOf(150.00))
                .build();

        assertFalse(rule.evaluate(stock));
    }

    @Test
    @DisplayName("Should return false when SMA200 is null")
    void shouldReturnFalseWhenSma200IsNull() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(200.00))
                .sma200(null)
                .build();

        assertFalse(rule.evaluate(stock));
    }

    @Test
    @DisplayName("Should return false when stock is null")
    void shouldReturnFalseWhenStockIsNull() {
        assertFalse(rule.evaluate(null));
    }

    @Test
    @DisplayName("Should have correct rule metadata")
    void shouldHaveCorrectRuleMetadata() {
        assertEquals("PRICE_ABOVE_SMA200", rule.getRuleId());
        assertEquals("Price Above SMA200", rule.getRuleName());
        assertNotNull(rule.getDescription());
    }
}
