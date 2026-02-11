package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.VolumeAboveAverageRule;

@DisplayName("VolumeAboveAverageRule Tests")
class VolumeAboveAverageRuleTest {

    private final VolumeAboveAverageRule rule = new VolumeAboveAverageRule();

    @Test
    @DisplayName("Should return true when volume is above average")
    void shouldReturnTrueWhenVolumeIsAboveAverage() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .volume(2000000L)
                .averageVolume(1000000L)
                .build();

        assertTrue(rule.evaluate(stock));
    }

    @Test
    @DisplayName("Should return false when volume is below average")
    void shouldReturnFalseWhenVolumeIsBelowAverage() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .volume(500000L)
                .averageVolume(1000000L)
                .build();

        assertFalse(rule.evaluate(stock));
    }

    @Test
    @DisplayName("Should return false when volume equals average")
    void shouldReturnFalseWhenVolumeEqualsAverage() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .volume(1000000L)
                .averageVolume(1000000L)
                .build();

        assertFalse(rule.evaluate(stock));
    }

    @Test
    @DisplayName("Should return false when volume is null")
    void shouldReturnFalseWhenVolumeIsNull() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .volume(null)
                .averageVolume(1000000L)
                .build();

        assertFalse(rule.evaluate(stock));
    }

    @Test
    @DisplayName("Should return false when average volume is null")
    void shouldReturnFalseWhenAverageVolumeIsNull() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .volume(2000000L)
                .averageVolume(null)
                .build();

        assertFalse(rule.evaluate(stock));
    }

    @Test
    @DisplayName("Should return false when average volume is zero")
    void shouldReturnFalseWhenAverageVolumeIsZero() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .volume(2000000L)
                .averageVolume(0L)
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
        assertEquals("VOLUME_ABOVE_AVERAGE", rule.getRuleId());
        assertEquals("Volume Above Average", rule.getRuleName());
        assertNotNull(rule.getDescription());
    }
}
