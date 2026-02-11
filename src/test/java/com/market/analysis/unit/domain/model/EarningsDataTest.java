package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.EarningsData;

@DisplayName("EarningsData Domain Model Tests")
class EarningsDataTest {

    @Test
    @DisplayName("Should create EarningsData with ticker and date")
    void shouldCreateEarningsDataWithTickerAndDate() {
        // Arrange & Act
        EarningsData earningsData = EarningsData.builder()
                .ticker("AAPL")
                .date("2024-04-25")
                .build();

        // Assert
        assertNotNull(earningsData);
        assertEquals("AAPL", earningsData.getTicker());
        assertEquals("2024-04-25", earningsData.getDate());
    }

    @Test
    @DisplayName("Should allow creating EarningsData with null values")
    void shouldAllowCreatingEarningsDataWithNullValues() {
        // Arrange & Act
        EarningsData earningsData = EarningsData.builder()
                .ticker(null)
                .date(null)
                .build();

        // Assert
        assertNotNull(earningsData);
        assertNull(earningsData.getTicker());
        assertNull(earningsData.getDate());
    }

    @Test
    @DisplayName("Should allow updating ticker and date")
    void shouldAllowUpdatingTickerAndDate() {
        // Arrange
        EarningsData earningsData = EarningsData.builder()
                .ticker("AAPL")
                .date("2024-04-25")
                .build();

        // Act
        earningsData.setTicker("GOOGL");
        earningsData.setDate("2024-05-15");

        // Assert
        assertEquals("GOOGL", earningsData.getTicker());
        assertEquals("2024-05-15", earningsData.getDate());
    }
}
