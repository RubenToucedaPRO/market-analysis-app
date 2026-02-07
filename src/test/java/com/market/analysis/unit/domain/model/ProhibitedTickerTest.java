package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.ProhibitedTicker;

/**
 * Unit tests for ProhibitedTicker domain model.
 */
@DisplayName("ProhibitedTicker Unit Tests")
class ProhibitedTickerTest {

    @Test
    @DisplayName("Should create ProhibitedTicker with ticker symbol")
    void testCreateProhibitedTicker() {
        // Arrange
        String ticker = "AAPL";

        // Act
        ProhibitedTicker prohibitedTicker = new ProhibitedTicker(ticker);

        // Assert
        assertNotNull(prohibitedTicker);
        assertEquals("AAPL", prohibitedTicker.getTicker());
    }

    @Test
    @DisplayName("Should create ProhibitedTicker with lowercase ticker")
    void testCreateProhibitedTickerWithLowercase() {
        // Arrange
        String ticker = "googl";

        // Act
        ProhibitedTicker prohibitedTicker = new ProhibitedTicker(ticker);

        // Assert
        assertNotNull(prohibitedTicker);
        assertEquals("googl", prohibitedTicker.getTicker());
    }

    @Test
    @DisplayName("Should create ProhibitedTicker with special characters in ticker")
    void testCreateProhibitedTickerWithSpecialCharacters() {
        // Arrange
        String ticker = "BRK.B";

        // Act
        ProhibitedTicker prohibitedTicker = new ProhibitedTicker(ticker);

        // Assert
        assertNotNull(prohibitedTicker);
        assertEquals("BRK.B", prohibitedTicker.getTicker());
    }
}
