package com.market.analysis.unit.domain.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.exception.StockDataNotFoundException;

@DisplayName("StockDataNotFoundException Tests")
class StockDataNotFoundExceptionTest {

    @Test
    @DisplayName("Should create exception with error code")
    void shouldCreateExceptionWithErrorCode() {
        // Arrange
        String errorCode = "ticker.not_found";
        
        // Act
        StockDataNotFoundException exception = new StockDataNotFoundException(errorCode);

        // Assert
        assertNotNull(exception);
        assertEquals(errorCode, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertArrayEquals(new Object[]{}, exception.getParams());
    }

    @Test
    @DisplayName("Should create exception with error code and params")
    void shouldCreateExceptionWithErrorCodeAndParams() {
        // Arrange
        String errorCode = "ticker.not_found";
        Object[] params = new Object[]{42L};
        
        // Act
        StockDataNotFoundException exception = new StockDataNotFoundException(errorCode, params);

        // Assert
        assertNotNull(exception);
        assertEquals(errorCode, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertArrayEquals(params, exception.getParams());
    }

    @Test
    @DisplayName("Should be throwable")
    void shouldBeThrowable() {
        // Arrange & Act & Assert
        assertThrows(StockDataNotFoundException.class, () -> {
            throw new StockDataNotFoundException("ticker.not_found");
        });
    }

    @Test
    @DisplayName("Should extend RuntimeException")
    void shouldExtendRuntimeException() {
        // Arrange & Act
        StockDataNotFoundException exception = new StockDataNotFoundException("ticker.not_found");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }
}
