package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.market.analysis.domain.model.ProhibitedTicker;

/**
 * Unit tests for ProhibitedTicker domain model.
 */
@DisplayName("ProhibitedTicker Unit Tests")
class ProhibitedTickerTest {

    @ParameterizedTest(name = "Caso {index}: Ticker = {0}")
    @CsvSource({
            "AAPL",
            "googl",
            "BRK.B"
    })
    @DisplayName("Should create ProhibitedTicker with various ticker formats")
    void testCreateProhibitedTicker(String ticker) {
        // Act
        ProhibitedTicker prohibitedTicker = new ProhibitedTicker(ticker);

        // Assert
        assertNotNull(prohibitedTicker, "The instance should not be null");
        assertEquals(ticker, prohibitedTicker.getTicker(), "The ticker should match the input");
    }
}
