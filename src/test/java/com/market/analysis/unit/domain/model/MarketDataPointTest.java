package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.MarketDataPoint;

/**
 * Unit tests for MarketDataPoint domain entity.
 */
@DisplayName("MarketDataPoint Domain Model Tests")
class MarketDataPointTest {

    @Test
    @DisplayName("Should create market data point with valid data")
    void testCreateMarketDataPointWithValidData() {
        // Arrange
        LocalDateTime date = LocalDateTime.of(2026, 2, 4, 10, 0);
        BigDecimal open = BigDecimal.valueOf(150.0);
        BigDecimal high = BigDecimal.valueOf(155.0);
        BigDecimal low = BigDecimal.valueOf(148.0);
        BigDecimal close = BigDecimal.valueOf(153.0);
        Long volume = 1000000L;

        // Act
        MarketDataPoint dataPoint = MarketDataPoint.builder()
                .date(date)
                .open(open)
                .high(high)
                .low(low)
                .close(close)
                .volume(volume)
                .build();

        // Assert
        assertNotNull(dataPoint);
        assertEquals(date, dataPoint.getDate());
        assertEquals(open, dataPoint.getOpen());
        assertEquals(high, dataPoint.getHigh());
        assertEquals(low, dataPoint.getLow());
        assertEquals(close, dataPoint.getClose());
        assertEquals(volume, dataPoint.getVolume());
    }

    @Test
    @DisplayName("Should create OHLCV data point representing a daily candle")
    void testCreateDailyCandleDataPoint() {
        // Arrange & Act
        MarketDataPoint dailyCandle = MarketDataPoint.builder()
                .date(LocalDateTime.of(2026, 2, 4, 0, 0))
                .open(BigDecimal.valueOf(100.50))
                .high(BigDecimal.valueOf(102.75))
                .low(BigDecimal.valueOf(99.25))
                .close(BigDecimal.valueOf(101.80))
                .volume(5000000L)
                .build();

        // Assert
        assertNotNull(dailyCandle);
        assertEquals(BigDecimal.valueOf(100.50), dailyCandle.getOpen());
        assertEquals(BigDecimal.valueOf(102.75), dailyCandle.getHigh());
        assertEquals(BigDecimal.valueOf(99.25), dailyCandle.getLow());
        assertEquals(BigDecimal.valueOf(101.80), dailyCandle.getClose());
    }

    @Test
    @DisplayName("Should create market data point with null values")
    void testCreateMarketDataPointWithNullValues() {
        // Act
        MarketDataPoint dataPoint = MarketDataPoint.builder()
                .date(null)
                .open(null)
                .high(null)
                .low(null)
                .close(null)
                .volume(null)
                .build();

        // Assert - Builder allows null values (validation would be done at business logic layer)
        assertNotNull(dataPoint);
    }

    @Test
    @DisplayName("Should create market data point for intraday period")
    void testCreateIntradayDataPoint() {
        // Arrange & Act - 5-minute candle
        MarketDataPoint intradayCandle = MarketDataPoint.builder()
                .date(LocalDateTime.of(2026, 2, 4, 9, 35))
                .open(BigDecimal.valueOf(150.25))
                .high(BigDecimal.valueOf(150.60))
                .low(BigDecimal.valueOf(150.10))
                .close(BigDecimal.valueOf(150.45))
                .volume(25000L)
                .build();

        // Assert
        assertNotNull(intradayCandle);
        assertEquals(LocalDateTime.of(2026, 2, 4, 9, 35), intradayCandle.getDate());
    }
}
