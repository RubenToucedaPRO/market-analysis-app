package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.MarketDataPoint;
import com.market.analysis.domain.model.TickerData;

/**
 * Unit tests for TickerData domain entity.
 */
@DisplayName("TickerData Domain Model Tests")
class TickerDataTest {

    @Test
    @DisplayName("Should create ticker data with valid data")
    void testCreateTickerDataWithValidData() {
        // Arrange
        String ticker = "AAPL";
        BigDecimal currentPrice = BigDecimal.valueOf(150.0);
        Long volume = 1000000L;
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, Object> indicators = Map.of("SMA20", 148.5, "RSI", 65.0);
        
        MarketDataPoint dataPoint = MarketDataPoint.builder()
                .date(LocalDateTime.now().minusDays(1))
                .open(BigDecimal.valueOf(149.0))
                .high(BigDecimal.valueOf(151.0))
                .low(BigDecimal.valueOf(148.0))
                .close(BigDecimal.valueOf(150.0))
                .volume(900000L)
                .build();
        
        List<MarketDataPoint> historicalData = List.of(dataPoint);

        // Act
        TickerData tickerData = TickerData.builder()
                .ticker(ticker)
                .currentPrice(currentPrice)
                .volume(volume)
                .timestamp(timestamp)
                .indicators(indicators)
                .historicalData(historicalData)
                .build();

        // Assert
        assertNotNull(tickerData);
        assertEquals(ticker, tickerData.getTicker());
        assertEquals(currentPrice, tickerData.getCurrentPrice());
        assertEquals(volume, tickerData.getVolume());
        assertEquals(timestamp, tickerData.getTimestamp());
        assertEquals(indicators, tickerData.getIndicators());
        assertEquals(1, tickerData.getHistoricalData().size());
    }

    @Test
    @DisplayName("Should return immutable copy of historical data list")
    void testGetHistoricalDataReturnsImmutableCopy() {
        // Arrange
        List<MarketDataPoint> historicalData = new ArrayList<>();
        historicalData.add(MarketDataPoint.builder()
                .date(LocalDateTime.now())
                .open(BigDecimal.valueOf(100.0))
                .high(BigDecimal.valueOf(101.0))
                .low(BigDecimal.valueOf(99.0))
                .close(BigDecimal.valueOf(100.5))
                .volume(1000L)
                .build());

        TickerData tickerData = TickerData.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.0))
                .volume(1000000L)
                .timestamp(LocalDateTime.now())
                .indicators(new HashMap<>())
                .historicalData(historicalData)
                .build();

        // Act
        List<MarketDataPoint> retrievedData = tickerData.getHistoricalData();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            retrievedData.add(MarketDataPoint.builder()
                    .date(LocalDateTime.now())
                    .open(BigDecimal.valueOf(100.0))
                    .high(BigDecimal.valueOf(101.0))
                    .low(BigDecimal.valueOf(99.0))
                    .close(BigDecimal.valueOf(100.5))
                    .volume(1000L)
                    .build());
        });
    }

    @Test
    @DisplayName("Should handle null historical data list in builder")
    void testBuilderWithNullHistoricalDataList() {
        // Act
        TickerData tickerData = TickerData.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.0))
                .volume(1000000L)
                .timestamp(LocalDateTime.now())
                .indicators(new HashMap<>())
                .historicalData(null)
                .build();

        // Assert
        assertNotNull(tickerData.getHistoricalData());
        assertTrue(tickerData.getHistoricalData().isEmpty());
    }

    @Test
    @DisplayName("Should create ticker data with multiple historical data points")
    void testCreateTickerDataWithMultipleHistoricalDataPoints() {
        // Arrange
        List<MarketDataPoint> historicalData = List.of(
            MarketDataPoint.builder()
                .date(LocalDateTime.now().minusDays(2))
                .open(BigDecimal.valueOf(145.0))
                .high(BigDecimal.valueOf(146.0))
                .low(BigDecimal.valueOf(144.0))
                .close(BigDecimal.valueOf(145.5))
                .volume(800000L)
                .build(),
            MarketDataPoint.builder()
                .date(LocalDateTime.now().minusDays(1))
                .open(BigDecimal.valueOf(145.5))
                .high(BigDecimal.valueOf(148.0))
                .low(BigDecimal.valueOf(145.0))
                .close(BigDecimal.valueOf(147.0))
                .volume(950000L)
                .build()
        );

        // Act
        TickerData tickerData = TickerData.builder()
                .ticker("GOOGL")
                .currentPrice(BigDecimal.valueOf(148.0))
                .volume(1000000L)
                .timestamp(LocalDateTime.now())
                .indicators(Map.of("SMA50", 140.0))
                .historicalData(historicalData)
                .build();

        // Assert
        assertEquals(2, tickerData.getHistoricalData().size());
        assertEquals(BigDecimal.valueOf(145.0), tickerData.getHistoricalData().get(0).getOpen());
        assertEquals(BigDecimal.valueOf(147.0), tickerData.getHistoricalData().get(1).getClose());
    }

    @Test
    @DisplayName("Should create ticker data with empty historical data")
    void testCreateTickerDataWithEmptyHistoricalData() {
        // Act
        TickerData tickerData = TickerData.builder()
                .ticker("MSFT")
                .currentPrice(BigDecimal.valueOf(300.0))
                .volume(2000000L)
                .timestamp(LocalDateTime.now())
                .indicators(new HashMap<>())
                .historicalData(List.of())
                .build();

        // Assert
        assertNotNull(tickerData.getHistoricalData());
        assertTrue(tickerData.getHistoricalData().isEmpty());
    }
}
