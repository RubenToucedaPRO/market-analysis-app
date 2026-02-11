package com.market.analysis.unit.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.exception.RuleDefinitionNotFoundException;

/**
 * Unit tests for RuleDefinitionNotFoundException.
 * Tests exception creation and message handling.
 */
@DisplayName("RuleDefinitionNotFoundException Tests")
class RuleDefinitionNotFoundExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void testExceptionWithMessage() {
        // Arrange
        String message = "Rule definition not found with id: 123";
        
        // Act
        RuleDefinitionNotFoundException exception = new RuleDefinitionNotFoundException(message);
        
        // Assert
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void testExceptionWithMessageAndCause() {
        // Arrange
        String message = "Rule definition not found";
        Throwable cause = new IllegalArgumentException("Invalid ID");
        
        // Act
        RuleDefinitionNotFoundException exception = new RuleDefinitionNotFoundException(message, cause);
        
        // Assert
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Invalid ID");
    }

    @Test
    @DisplayName("Should be throwable")
    void testExceptionIsThrowable() {
        // Arrange
        String message = "Test exception";
        
        // Act & Assert
        assertThatThrownBy(() -> {
            throw new RuleDefinitionNotFoundException(message);
        })
        .isInstanceOf(RuleDefinitionNotFoundException.class)
        .hasMessage(message);
    }
}
