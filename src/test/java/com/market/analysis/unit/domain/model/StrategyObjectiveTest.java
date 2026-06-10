package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.exception.DomainValidationException;
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
        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.target_type_null", exception.getErrorCode());
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
        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.stop_loss_type_null", exception.getErrorCode());
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
        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.target_value_null", exception.getErrorCode());
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
        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.stop_loss_value_null", exception.getErrorCode());
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
        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.capital_to_risk_null", exception.getErrorCode());
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
        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.description_null", exception.getErrorCode());
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
        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.description_null", exception.getErrorCode());
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
        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.target_value_zero", exception.getErrorCode());
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
        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.target_value_zero", exception.getErrorCode());
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
        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.stop_loss_value_zero", exception.getErrorCode());
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
        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.stop_loss_value_zero", exception.getErrorCode());
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
        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.capital_to_risk_zero", exception.getErrorCode());
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
        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.capital_to_risk_zero", exception.getErrorCode());
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

    // ---- Phase 3: SMA period validation via RuleCapabilityCatalog ----

    @Test
    @DisplayName("Should validate SMA target with valid period 20")
    void shouldValidateSmaTargetWithValidPeriod20() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(20))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("SMA20 target strategy")
                .build();

        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should validate SMA target with valid period 50")
    void shouldValidateSmaTargetWithValidPeriod50() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(50))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("SMA50 target strategy")
                .build();

        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should validate SMA target with valid period 200")
    void shouldValidateSmaTargetWithValidPeriod200() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(200))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("SMA200 target strategy")
                .build();

        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should validate SMA stop-loss with valid period 200")
    void shouldValidateSmaStopLossWithValidPeriod200() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.SMA)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(200))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("SMA200 stop-loss strategy")
                .build();

        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should throw exception when SMA target period is invalid")
    void shouldThrowExceptionWhenSmaTargetPeriodIsInvalid() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(100))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Invalid SMA target")
                .build();

        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.sma_period_invalid", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when SMA stop-loss period is invalid")
    void shouldThrowExceptionWhenSmaStopLossPeriodIsInvalid() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.SMA)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(15))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Invalid SMA stop-loss")
                .build();

        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.sma_period_invalid", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when both SMA target and stop-loss periods are invalid")
    void shouldThrowExceptionWhenBothSmaPeriodAreInvalid() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.SMA)
                .targetValue(BigDecimal.valueOf(99))
                .stopLossValue(BigDecimal.valueOf(300))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Both invalid SMA")
                .build();

        // Should fail on targetValue first
        DomainValidationException exception = assertThrows(DomainValidationException.class, objective::validate);
        assertEquals("validation.sma_period_invalid", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should not validate SMA period for non-SMA types")
    void shouldNotValidateSmaPeriodForNonSmaTypes() {
        // PERCENTAGE type with value 100 should be valid (not treated as SMA period)
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.FIXED_PRICE)
                .targetValue(BigDecimal.valueOf(100))
                .stopLossValue(BigDecimal.valueOf(95))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Non-SMA strategy with value 100")
                .build();

        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should validate mixed SMA and non-SMA types correctly")
    void shouldValidateMixedSmaAndNonSmaTypes() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(50))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Mixed valid strategy")
                .build();

        assertDoesNotThrow(objective::validate);
    }
}
