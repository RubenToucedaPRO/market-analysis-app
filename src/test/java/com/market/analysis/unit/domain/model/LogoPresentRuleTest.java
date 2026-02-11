package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.LogoPresentRule;
import com.market.analysis.domain.model.Stock;

@DisplayName("LogoPresentRule Tests")
class LogoPresentRuleTest {

    private final LogoPresentRule rule = new LogoPresentRule();

    @Test
    @DisplayName("Should return true when logo URL is present")
    void shouldReturnTrueWhenLogoUrlIsPresent() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .logoUrl("https://example.com/logo.png")
                .currentPrice(BigDecimal.valueOf(150.00))
                .build();

        assertTrue(rule.evaluate(stock));
    }

    @Test
    @DisplayName("Should return false when logo URL is null")
    void shouldReturnFalseWhenLogoUrlIsNull() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .logoUrl(null)
                .currentPrice(BigDecimal.valueOf(150.00))
                .build();

        assertFalse(rule.evaluate(stock));
    }

    @Test
    @DisplayName("Should return false when logo URL is empty")
    void shouldReturnFalseWhenLogoUrlIsEmpty() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .logoUrl("")
                .currentPrice(BigDecimal.valueOf(150.00))
                .build();

        assertFalse(rule.evaluate(stock));
    }

    @Test
    @DisplayName("Should return false when logo URL is blank")
    void shouldReturnFalseWhenLogoUrlIsBlank() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .logoUrl("   ")
                .currentPrice(BigDecimal.valueOf(150.00))
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
        assertEquals("LOGO_PRESENT", rule.getRuleId());
        assertEquals("Logo Present", rule.getRuleName());
        assertNotNull(rule.getDescription());
    }
}
