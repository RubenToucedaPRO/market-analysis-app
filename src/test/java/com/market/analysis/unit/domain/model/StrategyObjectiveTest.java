package com.market.analysis.unit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.ObjectiveType;
import com.market.analysis.domain.model.StrategyObjective;

/**
 * Unit tests for StrategyObjective domain model.
 */
@DisplayName("StrategyObjective Domain Model Tests")
class StrategyObjectiveTest {

    @Test
    @DisplayName("Should create StrategyObjective with SMA type")
    void shouldCreateStrategyObjectiveWithSmaType() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .type(ObjectiveType.SMA)
                .value(BigDecimal.valueOf(20))
                .build();

        // Assert
        assertThat(objective).isNotNull();
        assertThat(objective.getType()).isEqualTo(ObjectiveType.SMA);
        assertThat(objective.getValue()).isEqualByComparingTo("20");
    }

    @Test
    @DisplayName("Should create StrategyObjective with PERCENTAGE type")
    void shouldCreateStrategyObjectiveWithPercentageType() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .type(ObjectiveType.PERCENTAGE)
                .value(BigDecimal.valueOf(10.5))
                .build();

        // Assert
        assertThat(objective).isNotNull();
        assertThat(objective.getType()).isEqualTo(ObjectiveType.PERCENTAGE);
        assertThat(objective.getValue()).isEqualByComparingTo("10.5");
    }

    @Test
    @DisplayName("Should create StrategyObjective with FIXED_PRICE type")
    void shouldCreateStrategyObjectiveWithFixedPriceType() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .type(ObjectiveType.FIXED_PRICE)
                .value(BigDecimal.valueOf(150.75))
                .build();

        // Assert
        assertThat(objective).isNotNull();
        assertThat(objective.getType()).isEqualTo(ObjectiveType.FIXED_PRICE);
        assertThat(objective.getValue()).isEqualByComparingTo("150.75");
    }

    @Test
    @DisplayName("Should validate successfully with valid data")
    void shouldValidateSuccessfullyWithValidData() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .type(ObjectiveType.SMA)
                .value(BigDecimal.valueOf(50))
                .build();

        // Act & Assert - should not throw exception
        objective.validate();
    }

    @Test
    @DisplayName("Should throw exception when type is null")
    void shouldThrowExceptionWhenTypeIsNull() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .type(null)
                .value(BigDecimal.valueOf(50))
                .build();

        // Act & Assert
        assertThatThrownBy(() -> objective.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ObjectiveType cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when value is null")
    void shouldThrowExceptionWhenValueIsNull() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .type(ObjectiveType.SMA)
                .value(null)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> objective.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Objective value cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when value is negative")
    void shouldThrowExceptionWhenValueIsNegative() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .type(ObjectiveType.PERCENTAGE)
                .value(BigDecimal.valueOf(-5))
                .build();

        // Act & Assert
        assertThatThrownBy(() -> objective.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Objective value cannot be negative");
    }

    @Test
    @DisplayName("Should have proper toString representation")
    void shouldHaveProperToStringRepresentation() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .type(ObjectiveType.PERCENTAGE)
                .value(BigDecimal.valueOf(10))
                .build();

        // Act
        String toString = objective.toString();

        // Assert
        assertThat(toString).contains("PERCENTAGE");
        assertThat(toString).contains("10");
    }
}
