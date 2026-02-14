package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Stock;

@DisplayName("Stock Domain Model Tests")
class StockTest {

    @Test
    @DisplayName("Should create Stock with required fields")
    void shouldCreateStockWithRequiredFields() {
        // Arrange & Act
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.50))
                .build();

        // Assert
        assertNotNull(stock);
        assertEquals("AAPL", stock.getTicker());
        assertEquals(BigDecimal.valueOf(150.50), stock.getCurrentPrice());
    }

    @Test
    @DisplayName("Should create Stock with all fields")
    void shouldCreateStockWithAllFields() {
        // Arrange
        Instant now = Instant.now();

        // Act
        Stock stock = Stock.builder()
                .ticker("GOOGL")
                .logoUrl("https://example.com/logo.png")
                .currentPrice(BigDecimal.valueOf(2800.00))
                .openPrice(BigDecimal.valueOf(2750.00))
                .highOfDay(BigDecimal.valueOf(2850.00))
                .lowOfDay(BigDecimal.valueOf(2740.00))
                .previousClose(BigDecimal.valueOf(2745.00))
                .sma20(BigDecimal.valueOf(2780.00))
                .sma50(BigDecimal.valueOf(2760.00))
                .sma200(BigDecimal.valueOf(2700.00))
                .volume(1000000L)
                .averageVolume(950000L)
                .lastUpdated(now)
                .build();

        // Assert
        assertNotNull(stock);
        assertEquals("GOOGL", stock.getTicker());
        assertEquals("https://example.com/logo.png", stock.getLogoUrl());
        assertEquals(BigDecimal.valueOf(2800.00), stock.getCurrentPrice());
        assertEquals(BigDecimal.valueOf(2750.00), stock.getOpenPrice());
        assertEquals(BigDecimal.valueOf(2850.00), stock.getHighOfDay());
        assertEquals(BigDecimal.valueOf(2740.00), stock.getLowOfDay());
        assertEquals(BigDecimal.valueOf(2745.00), stock.getPreviousClose());
        assertEquals(BigDecimal.valueOf(2780.00), stock.getSma20());
        assertEquals(BigDecimal.valueOf(2760.00), stock.getSma50());
        assertEquals(BigDecimal.valueOf(2700.00), stock.getSma200());
        assertEquals(1000000L, stock.getVolume());
        assertEquals(950000L, stock.getAverageVolume());
        assertEquals(now, stock.getLastUpdated());
    }

    @Test
    @DisplayName("Should allow setting optional fields after creation")
    void shouldAllowSettingOptionalFieldsAfterCreation() {
        // Arrange
        Stock stock = Stock.builder()
                .ticker("TSLA")
                .currentPrice(BigDecimal.valueOf(700.00))
                .build();

        // Act
        stock.setLogoUrl("https://example.com/tesla-logo.png");
        stock.setVolume(2000000L);
        stock.setSma20(BigDecimal.valueOf(705.00));

        // Assert
        assertEquals("https://example.com/tesla-logo.png", stock.getLogoUrl());
        assertEquals(2000000L, stock.getVolume());
        assertEquals(BigDecimal.valueOf(705.00), stock.getSma20());
    }

    @Test
    @DisplayName("Should create Stock with ticker containing special characters")
    void shouldCreateStockWithSpecialCharactersInTicker() {
        // Arrange & Act
        Stock stock = Stock.builder()
                .ticker("BRK.B")
                .currentPrice(BigDecimal.valueOf(450.00))
                .build();

        // Assert
        assertNotNull(stock);
        assertEquals("BRK.B", stock.getTicker());
    }
}
