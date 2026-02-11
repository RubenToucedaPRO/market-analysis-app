package com.market.analysis.unit.infrastructure.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.infrastructure.exception.StockException;

/**
 * Unit tests for StockException.
 */
@DisplayName("StockException Tests")
class StockExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void testExceptionWithMessage() {
        // Arrange
        String message = "Stock data not found for ticker: AAPL";
        
        // Act
        StockException exception = new StockException(message);
        
        // Assert
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void testExceptionWithMessageAndCause() {
        // Arrange
        String message = "Failed to fetch stock data";
        Throwable cause = new IllegalArgumentException("Invalid ticker");
        
        // Act
        StockException exception = new StockException(message, cause);
        
        // Assert
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Invalid ticker");
    }

    @Test
    @DisplayName("Should be throwable")
    void testExceptionIsThrowable() {
        // Arrange
        String message = "Test stock exception";
        
        // Act & Assert
        assertThatThrownBy(() -> {
            throw new StockException(message);
        })
        .isInstanceOf(StockException.class)
        .hasMessage(message);
    }

    @Test
    @DisplayName("Should preserve cause chain")
    void testExceptionCauseChain() {
        // Arrange
        Throwable rootCause = new NullPointerException("Null ticker");
        Throwable cause = new IllegalArgumentException("Invalid input", rootCause);
        String message = "Stock operation failed";
        
        // Act
        StockException exception = new StockException(message, cause);
        
        // Assert
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
    }
}
