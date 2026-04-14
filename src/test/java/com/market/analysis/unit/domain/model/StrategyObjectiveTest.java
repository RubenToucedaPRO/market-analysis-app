package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.ObjectiveType;
import com.market.analysis.domain.model.StrategyObjective;

@DisplayName("StrategyObjective Domain Model Tests")
class StrategyObjectiveTest {

    @Test
    @DisplayName("Should create valid StrategyObjective with all fields")
    void shouldCreateValidStrategyObjectiveWithAllFields() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Conservative growth strategy with 5% target and 2% stop-loss")
                .build();

        // Assert
        assertNotNull(objective);
        assertEquals(ObjectiveType.PERCENTAGE, objective.getTargetType());
        assertEquals(ObjectiveType.PERCENTAGE, objective.getStopLossType());
        assertEquals(BigDecimal.valueOf(5.0), objective.getTargetValue());
        assertEquals(BigDecimal.valueOf(2.0), objective.getStopLossValue());
        assertEquals(BigDecimal.valueOf(0.02), objective.getCapitalToRisk());
        assertEquals("Conservative growth strategy with 5% target and 2% stop-loss", objective.getDescription());
        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should create StrategyObjective with SMA type")
    void shouldCreateStrategyObjectiveWithSMAType() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.SMA)
                .targetValue(BigDecimal.valueOf(20))
                .stopLossValue(BigDecimal.valueOf(50))
                .capitalToRisk(BigDecimal.valueOf(0.01))
                .description("SMA-based strategy with 20-period target and 50-period stop-loss")
                .build();

        // Assert
        assertEquals(ObjectiveType.SMA, objective.getTargetType());
        assertEquals(ObjectiveType.SMA, objective.getStopLossType());
        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should create StrategyObjective with FIXED_PRICE type")
    void shouldCreateStrategyObjectiveWithFixedPriceType() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.FIXED_PRICE)
                .stopLossType(ObjectiveType.FIXED_PRICE)
                .targetValue(BigDecimal.valueOf(150.00))
                .stopLossValue(BigDecimal.valueOf(140.00))
                .capitalToRisk(BigDecimal.valueOf(0.05))
                .description("Fixed price strategy with $150 target and $140 stop-loss")
                .build();

        // Assert
        assertEquals(ObjectiveType.FIXED_PRICE, objective.getTargetType());
        assertEquals(ObjectiveType.FIXED_PRICE, objective.getStopLossType());
        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should create StrategyObjective with mixed objective types")
    void shouldCreateStrategyObjectiveWithMixedObjectiveTypes() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.FIXED_PRICE)
                .targetValue(BigDecimal.valueOf(10.0))
                .stopLossValue(BigDecimal.valueOf(95.00))
                .capitalToRisk(BigDecimal.valueOf(0.03))
                .description("Mixed strategy: 10% target, $95 fixed stop-loss")
                .build();

        // Assert
        assertEquals(ObjectiveType.PERCENTAGE, objective.getTargetType());
        assertEquals(ObjectiveType.FIXED_PRICE, objective.getStopLossType());
        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should throw exception when targetType is null")
    void shouldThrowExceptionWhenTargetTypeIsNull() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(null)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("targetType cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when stopLossType is null")
    void shouldThrowExceptionWhenStopLossTypeIsNull() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(null)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("stopLossType cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when targetValue is null")
    void shouldThrowExceptionWhenTargetValueIsNull() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(null)
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("targetValue cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when stopLossValue is null")
    void shouldThrowExceptionWhenStopLossValueIsNull() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(null)
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("stopLossValue cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when capitalToRisk is null")
    void shouldThrowExceptionWhenCapitalToRiskIsNull() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(null)
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("capitalToRisk cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when description is null")
    void shouldThrowExceptionWhenDescriptionIsNull() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description(null)
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("description cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when description is blank")
    void shouldThrowExceptionWhenDescriptionIsBlank() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("   ")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("description cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when targetValue is zero")
    void shouldThrowExceptionWhenTargetValueIsZero() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.ZERO)
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("targetValue must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when targetValue is negative")
    void shouldThrowExceptionWhenTargetValueIsNegative() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(-5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("targetValue must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when stopLossValue is zero")
    void shouldThrowExceptionWhenStopLossValueIsZero() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.ZERO)
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("stopLossValue must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when stopLossValue is negative")
    void shouldThrowExceptionWhenStopLossValueIsNegative() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(-2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("stopLossValue must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when capitalToRisk is zero")
    void shouldThrowExceptionWhenCapitalToRiskIsZero() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.ZERO)
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("capitalToRisk must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when capitalToRisk is negative")
    void shouldThrowExceptionWhenCapitalToRiskIsNegative() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(-0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("capitalToRisk must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should maintain immutability of all fields")
    void shouldMaintainImmutabilityOfAllFields() {
        // Arrange
        ObjectiveType originalTargetType = ObjectiveType.PERCENTAGE;
        ObjectiveType originalStopLossType = ObjectiveType.FIXED_PRICE;
        BigDecimal originalTargetValue = BigDecimal.valueOf(10.0);
        BigDecimal originalStopLossValue = BigDecimal.valueOf(100.0);
        BigDecimal originalCapitalToRisk = BigDecimal.valueOf(0.03);
        String originalDescription = "Test strategy";

        // Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(originalTargetType)
                .stopLossType(originalStopLossType)
                .targetValue(originalTargetValue)
                .stopLossValue(originalStopLossValue)
                .capitalToRisk(originalCapitalToRisk)
                .description(originalDescription)
                .build();

        // Assert - Values should remain unchanged
        assertEquals(originalTargetType, objective.getTargetType());
        assertEquals(originalStopLossType, objective.getStopLossType());
        assertEquals(originalTargetValue, objective.getTargetValue());
        assertEquals(originalStopLossValue, objective.getStopLossValue());
        assertEquals(originalCapitalToRisk, objective.getCapitalToRisk());
        assertEquals(originalDescription, objective.getDescription());
    }

    @Test
    @DisplayName("Should have proper toString implementation")
    void shouldHaveProperToStringImplementation() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        String toString = objective.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("targetType"));
        assertTrue(toString.contains("PERCENTAGE"));
        assertTrue(toString.contains("targetValue"));
        assertTrue(toString.contains("5.0"));
    }

    @Test
    @DisplayName("Should throw exception when targetType is SMA and targetValue is not 20, 50, or 200")
    void shouldThrowExceptionWhenSmaTargetValueIsInvalid() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(100))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertTrue(exception.getMessage().contains("targetValue"));
        assertTrue(exception.getMessage().contains("SMA"));
    }

    @Test
    @DisplayName("Should throw exception when stopLossType is SMA and stopLossValue is not 20, 50, or 200")
    void shouldThrowExceptionWhenSmaStopLossValueIsInvalid() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.SMA)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(100))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertTrue(exception.getMessage().contains("stopLossValue"));
        assertTrue(exception.getMessage().contains("SMA"));
    }

    @Test
    @DisplayName("Should accept SMA targetValue of 20")
    void shouldAcceptSmaTargetValue20() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(20))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("SMA 20 strategy")
                .build();

        // Act & Assert
        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should accept SMA targetValue of 50")
    void shouldAcceptSmaTargetValue50() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(50))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("SMA 50 strategy")
                .build();

        // Act & Assert
        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should accept SMA targetValue of 200")
    void shouldAcceptSmaTargetValue200() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(200))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("SMA 200 strategy")
                .build();

        // Act & Assert
        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should accept SMA stopLossValue of 20, 50, and 200")
    void shouldAcceptSmaStopLossValidValues() {
        for (int period : new int[]{20, 50, 200}) {
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .stopLossType(ObjectiveType.SMA)
                    .targetValue(BigDecimal.valueOf(5.0))
                    .stopLossValue(BigDecimal.valueOf(period))
                    .capitalToRisk(BigDecimal.valueOf(0.02))
                    .description("SMA " + period + " stop-loss strategy")
                    .build();

            assertDoesNotThrow(objective::validate, "Should accept SMA period " + period);
        }
    }

    @Test
    @DisplayName("Should reject SMA targetValue of 10")
    void shouldRejectSmaTargetValue10() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(10))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Invalid SMA period")
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, objective::validate);
    }

    @Test
    @DisplayName("Should accept very small positive BigDecimal values")
    void shouldAcceptVerySmallPositiveBigDecimalValues() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(0.0001))
                .stopLossValue(BigDecimal.valueOf(0.0001))
                .capitalToRisk(BigDecimal.valueOf(0.0001))
                .description("Micro-adjustment strategy")
                .build();

        // Assert
        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should accept very large BigDecimal values")
    void shouldAcceptVeryLargeBigDecimalValues() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.FIXED_PRICE)
                .stopLossType(ObjectiveType.FIXED_PRICE)
                .targetValue(BigDecimal.valueOf(1000000.0))
                .stopLossValue(BigDecimal.valueOf(999000.0))
                .capitalToRisk(BigDecimal.valueOf(100.0))
                .description("High-value strategy")
                .build();

        // Assert
        assertDoesNotThrow(objective::validate);
    }
}
