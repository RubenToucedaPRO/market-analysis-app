package com.market.analysis.unit.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.exception.MissingIndicatorException;

/**
 * Unit tests for MissingIndicatorException.
 */
@DisplayName("MissingIndicatorException Tests")
class MissingIndicatorExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
        // Arrange
        String message = "SMA20 is not available for ticker AAPL";

        // Act
        MissingIndicatorException exception = new MissingIndicatorException(message);

        // Assert
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        // Arrange
        String message = "SMA50 is not available";
        Throwable cause = new IllegalStateException("Indicator not calculated");

        // Act
        MissingIndicatorException exception = new MissingIndicatorException(message, cause);

        // Assert
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Should be instance of RuntimeException")
    void shouldBeInstanceOfRuntimeException() {
        // Arrange & Act
        MissingIndicatorException exception = new MissingIndicatorException("Test message");

        // Assert
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
