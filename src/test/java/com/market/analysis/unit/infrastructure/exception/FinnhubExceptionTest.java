package com.market.analysis.unit.infrastructure.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.infrastructure.exception.FinnhubException;

/**
 * Unit tests for FinnhubException.
 */
@DisplayName("FinnhubException Tests")
class FinnhubExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void testExceptionWithMessage() {
        // Arrange
        String message = "Finnhub API rate limit exceeded";
        
        // Act
        FinnhubException exception = new FinnhubException(message);
        
        // Assert
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void testExceptionWithMessageAndCause() {
        // Arrange
        String message = "Failed to fetch data from Finnhub";
        Throwable cause = new java.io.IOException("Connection timeout");
        
        // Act
        FinnhubException exception = new FinnhubException(message, cause);
        
        // Assert
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Connection timeout");
    }

    @Test
    @DisplayName("Should be throwable")
    void testExceptionIsThrowable() {
        // Arrange
        String message = "Test Finnhub exception";
        
        // Act & Assert
        assertThatThrownBy(() -> {
            throw new FinnhubException(message);
        })
        .isInstanceOf(FinnhubException.class)
        .hasMessage(message);
    }

    @Test
    @DisplayName("Should preserve cause chain")
    void testExceptionCauseChain() {
        // Arrange
        Throwable rootCause = new java.net.SocketTimeoutException("Read timeout");
        Throwable cause = new java.io.IOException("Network error", rootCause);
        String message = "Finnhub API call failed";
        
        // Act
        FinnhubException exception = new FinnhubException(message, cause);
        
        // Assert
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
    }
}
