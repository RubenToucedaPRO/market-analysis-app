package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Candle;

/**
 * Unit tests for MarketDataPoint domain entity.
 */
@DisplayName("MarketDataPoint Domain Model Tests")
class MarketDataPointTest {

    @Test
    @DisplayName("Should create market data point with valid data")
    void testCreateMarketDataPointWithValidData() {
        // Arrange
        Instant date = Instant.now();
        BigDecimal open = BigDecimal.valueOf(150.0);
        BigDecimal high = BigDecimal.valueOf(155.0);
        BigDecimal low = BigDecimal.valueOf(148.0);
        BigDecimal close = BigDecimal.valueOf(153.0);
        Long volume = 1000000L;

        // Act
        Candle dataPoint = Candle.builder()
                .dateTime(date)
                .openPrice(open)
                .highPrice(high)
                .lowPrice(low)
                .closePrice(close)
                .volume(volume)
                .build();

        // Assert
        assertNotNull(dataPoint);
        assertEquals(date, dataPoint.getDateTime());
        assertEquals(open, dataPoint.getOpenPrice());
        assertEquals(high, dataPoint.getHighPrice());
        assertEquals(low, dataPoint.getLowPrice());
        assertEquals(close, dataPoint.getClosePrice());
        assertEquals(volume, dataPoint.getVolume());
    }

    @Test
    @DisplayName("Should create OHLCV data point representing a daily candle")
    void testCreateDailyCandleDataPoint() {
        // Arrange & Act
        Candle dailyCandle = Candle.builder()
                .dateTime(Instant.now())
                .openPrice(BigDecimal.valueOf(100.50))
                .highPrice(BigDecimal.valueOf(102.75))
                .lowPrice(BigDecimal.valueOf(99.25))
                .closePrice(BigDecimal.valueOf(101.80))
                .volume(5000000L)
                .build();

        // Assert
        assertNotNull(dailyCandle);
        assertEquals(BigDecimal.valueOf(100.50), dailyCandle.getOpenPrice());
        assertEquals(BigDecimal.valueOf(102.75), dailyCandle.getHighPrice());
        assertEquals(BigDecimal.valueOf(99.25), dailyCandle.getLowPrice());
        assertEquals(BigDecimal.valueOf(101.80), dailyCandle.getClosePrice());
    }

    @Test
    @DisplayName("Should create market data point with null values")
    void testCreateMarketDataPointWithNullValues() {
        // Act
        Candle dataPoint = Candle.builder()
                .dateTime(null)
                .openPrice(null)
                .highPrice(null)
                .lowPrice(null)
                .closePrice(null)
                .volume(null)
                .build();

        // Assert - Builder allows null values (validation would be done at business
        // logic layer)
        assertNotNull(dataPoint);
    }

    @Test
    @DisplayName("Should create market data point for intraday period")
    void testCreateIntradayDataPoint() {
        // Arrange & Act - 5-minute candle
        Candle intradayCandle = Candle.builder()
                .dateTime(Instant.now())
                .openPrice(BigDecimal.valueOf(150.25))
                .highPrice(BigDecimal.valueOf(150.60))
                .lowPrice(BigDecimal.valueOf(150.10))
                .closePrice(BigDecimal.valueOf(150.45))
                .volume(25000L)
                .build();

        // Assert
        assertNotNull(intradayCandle);
        assertEquals(Instant.now().getEpochSecond(), intradayCandle.getDateTime().getEpochSecond());
    }
}
