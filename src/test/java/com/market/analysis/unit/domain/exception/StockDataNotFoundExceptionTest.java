package com.market.analysis.unit.domain.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.exception.StockDataNotFoundException;

@DisplayName("StockDataNotFoundException Tests")
class StockDataNotFoundExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
        // Arrange
        String message = "Ticker data not found for: AAPL";
        
        // Act
        StockDataNotFoundException exception = new StockDataNotFoundException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should be throwable")
    void shouldBeThrowable() {
        // Arrange & Act & Assert
        assertThrows(StockDataNotFoundException.class, () -> {
            throw new StockDataNotFoundException("Test exception");
        });
    }

    @Test
    @DisplayName("Should extend RuntimeException")
    void shouldExtendRuntimeException() {
        // Arrange & Act
        StockDataNotFoundException exception = new StockDataNotFoundException("Test");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }
}
