package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Candle;

@DisplayName("Candle Domain Model Tests")
class CandleTest {

    @Test
    @DisplayName("Should create Candle with all OHLCV data")
    void shouldCreateCandleWithAllOHLCVData() {
        // Arrange
        Instant dateTime = Instant.now();
        // Act
        Candle candle = Candle.builder()
                .ticker("AAPL")
                .dateTime(dateTime)
                .openPrice(BigDecimal.valueOf(150.00))
                .highPrice(BigDecimal.valueOf(152.50))
                .lowPrice(BigDecimal.valueOf(149.50))
                .closePrice(BigDecimal.valueOf(151.00))
                .volume(1000000L)
                .build();

        // Assert
        assertNotNull(candle);
        assertEquals("AAPL", candle.getTicker());
        assertEquals(dateTime, candle.getDateTime());
        assertEquals(BigDecimal.valueOf(150.00), candle.getOpenPrice());
        assertEquals(BigDecimal.valueOf(152.50), candle.getHighPrice());
        assertEquals(BigDecimal.valueOf(149.50), candle.getLowPrice());
        assertEquals(BigDecimal.valueOf(151.00), candle.getClosePrice());
        assertEquals(1000000L, candle.getVolume());
    }

    @Test
    @DisplayName("Should create Candle with ticker containing special characters")
    void shouldCreateCandleWithSpecialCharactersInTicker() {
        // Arrange
        Instant dateTime = Instant.now();

        // Act
        Candle candle = Candle.builder()
                .ticker("BRK.B")
                .dateTime(dateTime)
                .openPrice(BigDecimal.valueOf(300.00))
                .highPrice(BigDecimal.valueOf(305.00))
                .lowPrice(BigDecimal.valueOf(299.00))
                .closePrice(BigDecimal.valueOf(303.00))
                .volume(500000L)
                .build();

        // Assert
        assertNotNull(candle);
        assertEquals("BRK.B", candle.getTicker());
    }

    @Test
    @DisplayName("Should create Candle with large volume")
    void shouldCreateCandleWithLargeVolume() {
        // Arrange
        Instant dateTime = Instant.now();

        // Act
        Candle candle = Candle.builder()
                .ticker("TSLA")
                .dateTime(dateTime)
                .openPrice(BigDecimal.valueOf(700.00))
                .highPrice(BigDecimal.valueOf(720.00))
                .lowPrice(BigDecimal.valueOf(695.00))
                .closePrice(BigDecimal.valueOf(715.00))
                .volume(50000000L)
                .build();

        // Assert
        assertEquals(50000000L, candle.getVolume());
    }

    @Test
    @DisplayName("Should create Candle with decimal prices")
    void shouldCreateCandleWithDecimalPrices() {
        // Arrange
        Instant dateTime = Instant.now();

        // Act
        Candle candle = Candle.builder()
                .ticker("GOOGL")
                .dateTime(dateTime)
                .openPrice(BigDecimal.valueOf(2800.50))
                .highPrice(BigDecimal.valueOf(2850.75))
                .lowPrice(BigDecimal.valueOf(2795.25))
                .closePrice(BigDecimal.valueOf(2840.00))
                .volume(1500000L)
                .build();

        // Assert
        assertEquals(BigDecimal.valueOf(2800.50), candle.getOpenPrice());
        assertEquals(BigDecimal.valueOf(2850.75), candle.getHighPrice());
        assertEquals(BigDecimal.valueOf(2795.25), candle.getLowPrice());
        assertEquals(BigDecimal.valueOf(2840.00), candle.getClosePrice());
    }
}
