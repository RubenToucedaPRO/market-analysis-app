package com.market.analysis.unit.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.exception.InvalidRiskRewardException;

/**
 * Unit tests for InvalidRiskRewardException.
 */
@DisplayName("InvalidRiskRewardException Tests")
class InvalidRiskRewardExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
        // Arrange
        String message = "Stop-loss price must be less than entry price";

        // Act
        InvalidRiskRewardException exception = new InvalidRiskRewardException(message);

        // Assert
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        // Arrange
        String message = "Invalid risk/reward configuration";
        Throwable cause = new IllegalArgumentException("Invalid parameters");

        // Act
        InvalidRiskRewardException exception = new InvalidRiskRewardException(message, cause);

        // Assert
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Should be instance of RuntimeException")
    void shouldBeInstanceOfRuntimeException() {
        // Arrange & Act
        InvalidRiskRewardException exception = new InvalidRiskRewardException("Test message");

        // Assert
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
