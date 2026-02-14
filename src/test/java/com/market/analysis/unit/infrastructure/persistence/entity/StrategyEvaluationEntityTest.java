package com.market.analysis.unit.infrastructure.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.market.analysis.infrastructure.persistence.entity.StockEntity;
import com.market.analysis.infrastructure.persistence.entity.StrategyEvaluationEntity;

/**
 * Unit tests for StrategyEvaluationEntity JPA entity.
 * Validates getters, setters, and field constraints.
 */
@DisplayName("StrategyEvaluationEntity Unit Tests")
class StrategyEvaluationEntityTest {

    @Nested
    @DisplayName("Entity Creation Tests")
    class EntityCreationTests {

        @Test
        @DisplayName("Should create entity with default constructor")
        void shouldCreateEntityWithDefaultConstructor() {
            // Act
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();

            // Assert
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isNull();
            assertThat(entity.getStock()).isNull();
        }

        @Test
        @DisplayName("Should create entity and set all fields")
        void shouldCreateEntityAndSetAllFields() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            StockEntity stock = new StockEntity();
            stock.setId(1L);
            stock.setTicker("AAPL");
            stock.setStrategyId(10L);
            Instant evaluatedAt = Instant.now();

            // Act
            entity.setId(1L);
            entity.setStock(stock);
            entity.setCompliant(true);
            entity.setComplianceRate(BigDecimal.valueOf(85.50));
            entity.setSummary("Strategy passed with 85.50% compliance");
            entity.setEvaluatedAt(evaluatedAt);
            entity.setPriceAtEvaluation(BigDecimal.valueOf(150.25));
            entity.setLatest(true);

            // Assert
            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getStock()).isNotNull();
            assertThat(entity.getStock().getTicker()).isEqualTo("AAPL");
            assertThat(entity.getStock().getStrategyId()).isEqualTo(10L);
            assertThat(entity.isCompliant()).isTrue();
            assertThat(entity.getComplianceRate()).isEqualByComparingTo(BigDecimal.valueOf(85.50));
            assertThat(entity.getSummary()).isEqualTo("Strategy passed with 85.50% compliance");
            assertThat(entity.getEvaluatedAt()).isEqualTo(evaluatedAt);
            assertThat(entity.getPriceAtEvaluation()).isEqualByComparingTo(BigDecimal.valueOf(150.25));
            assertThat(entity.isLatest()).isTrue();
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set id")
        void shouldGetAndSetId() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();

            // Act
            entity.setId(100L);

            // Assert
            assertThat(entity.getId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("Should get and set stock")
        void shouldGetAndSetStock() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            StockEntity stock = new StockEntity();
            stock.setId(1L);
            stock.setTicker("GOOGL");

            // Act
            entity.setStock(stock);

            // Assert
            assertThat(entity.getStock()).isNotNull();
            assertThat(entity.getStock().getTicker()).isEqualTo("GOOGL");
        }

        @Test
        @DisplayName("Should get and set compliant flag")
        void shouldGetAndSetCompliantFlag() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();

            // Act
            entity.setCompliant(false);

            // Assert
            assertThat(entity.isCompliant()).isFalse();
        }

        @Test
        @DisplayName("Should get and set complianceRate")
        void shouldGetAndSetComplianceRate() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            BigDecimal rate = BigDecimal.valueOf(92.75);

            // Act
            entity.setComplianceRate(rate);

            // Assert
            assertThat(entity.getComplianceRate()).isEqualByComparingTo(rate);
        }

        @Test
        @DisplayName("Should get and set summary")
        void shouldGetAndSetSummary() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            String summary = "Excellent performance with all rules passed";

            // Act
            entity.setSummary(summary);

            // Assert
            assertThat(entity.getSummary()).isEqualTo(summary);
        }

        @Test
        @DisplayName("Should get and set evaluatedAt timestamp")
        void shouldGetAndSetEvaluatedAtTimestamp() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            Instant timestamp = Instant.now();

            // Act
            entity.setEvaluatedAt(timestamp);

            // Assert
            assertThat(entity.getEvaluatedAt()).isEqualTo(timestamp);
        }

        @Test
        @DisplayName("Should get and set priceAtEvaluation")
        void shouldGetAndSetPriceAtEvaluation() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            BigDecimal price = BigDecimal.valueOf(2800.50);

            // Act
            entity.setPriceAtEvaluation(price);

            // Assert
            assertThat(entity.getPriceAtEvaluation()).isEqualByComparingTo(price);
        }

        @Test
        @DisplayName("Should get and set latest flag")
        void shouldGetAndSetLatestFlag() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();

            // Act
            entity.setLatest(true);

            // Assert
            assertThat(entity.isLatest()).isTrue();
        }
    }

    @Nested
    @DisplayName("Field Validation Tests")
    class FieldValidationTests {

        @Test
        @DisplayName("Should handle null optional fields")
        void shouldHandleNullOptionalFields() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();

            // Act
            entity.setCompliant(true);
            entity.setComplianceRate(BigDecimal.valueOf(100.00));
            entity.setEvaluatedAt(Instant.now());
            entity.setLatest(true);
            // id, stock, summary, priceAtEvaluation are null

            // Assert
            assertThat(entity.getId()).isNull();
            assertThat(entity.getStock()).isNull();
            assertThat(entity.getSummary()).isNull();
            assertThat(entity.getPriceAtEvaluation()).isNull();
            assertThat(entity.isCompliant()).isTrue();
        }

        @Test
        @DisplayName("Should handle zero compliance rate")
        void shouldHandleZeroComplianceRate() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();

            // Act
            entity.setComplianceRate(BigDecimal.ZERO);

            // Assert
            assertThat(entity.getComplianceRate()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle 100% compliance rate")
        void shouldHandle100PercentComplianceRate() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();

            // Act
            entity.setComplianceRate(BigDecimal.valueOf(100.00));

            // Assert
            assertThat(entity.getComplianceRate()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        }

        @Test
        @DisplayName("Should handle very long summary text")
        void shouldHandleVeryLongSummaryText() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            String longSummary = "A".repeat(2000); // Max length = 2000

            // Act
            entity.setSummary(longSummary);

            // Assert
            assertThat(entity.getSummary()).hasSize(2000);
        }

        @Test
        @DisplayName("Should handle negative price at evaluation")
        void shouldHandleNegativePriceAtEvaluation() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();

            // Act
            entity.setPriceAtEvaluation(BigDecimal.valueOf(-10.00));

            // Assert
            assertThat(entity.getPriceAtEvaluation()).isEqualByComparingTo(BigDecimal.valueOf(-10.00));
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should represent passed evaluation correctly")
        void shouldRepresentPassedEvaluationCorrectly() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            StockEntity stock = new StockEntity();
            stock.setTicker("MSFT");
            stock.setStrategyId(10L);

            // Act
            entity.setStock(stock);
            entity.setCompliant(true);
            entity.setComplianceRate(BigDecimal.valueOf(90.00));
            entity.setSummary("All rules passed successfully");
            entity.setEvaluatedAt(Instant.now());
            entity.setPriceAtEvaluation(BigDecimal.valueOf(380.00));
            entity.setLatest(true);

            // Assert
            assertThat(entity.isCompliant()).isTrue();
            assertThat(entity.getComplianceRate()).isGreaterThan(BigDecimal.valueOf(80.00));
            assertThat(entity.isLatest()).isTrue();
        }

        @Test
        @DisplayName("Should represent failed evaluation correctly")
        void shouldRepresentFailedEvaluationCorrectly() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            StockEntity stock = new StockEntity();
            stock.setTicker("AMZN");
            stock.setStrategyId(15L);

            // Act
            entity.setStock(stock);
            entity.setCompliant(false);
            entity.setComplianceRate(BigDecimal.valueOf(35.00));
            entity.setSummary("Strategy failed - insufficient rules passed");
            entity.setEvaluatedAt(Instant.now());
            entity.setPriceAtEvaluation(BigDecimal.valueOf(3200.00));
            entity.setLatest(false);

            // Assert
            assertThat(entity.isCompliant()).isFalse();
            assertThat(entity.getComplianceRate()).isLessThan(BigDecimal.valueOf(50.00));
            assertThat(entity.isLatest()).isFalse();
        }

        @Test
        @DisplayName("Should represent historical evaluation (not latest)")
        void shouldRepresentHistoricalEvaluation() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            StockEntity stock = new StockEntity();
            stock.setTicker("NVDA");
            stock.setStrategyId(20L);
            Instant pastDate = Instant.now().minusSeconds(3600); // 1 hour in the past

            // Act
            entity.setStock(stock);
            entity.setCompliant(true);
            entity.setComplianceRate(BigDecimal.valueOf(88.00));
            entity.setEvaluatedAt(pastDate);
            entity.setLatest(false);

            // Assert
            assertThat(entity.isLatest()).isFalse();
            assertThat(entity.getEvaluatedAt()).isBefore(Instant.now());
        }

        @Test
        @DisplayName("Should represent current evaluation (latest)")
        void shouldRepresentCurrentEvaluation() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            StockEntity stock = new StockEntity();
            stock.setTicker("META");
            stock.setStrategyId(30L);
            Instant now = Instant.now();

            // Act
            entity.setStock(stock);
            entity.setCompliant(true);
            entity.setComplianceRate(BigDecimal.valueOf(95.00));
            entity.setEvaluatedAt(now);
            entity.setLatest(true);

            // Assert
            assertThat(entity.isLatest()).isTrue();
            assertThat(entity.getEvaluatedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should maintain data consistency after multiple updates")
        void shouldMaintainDataConsistencyAfterMultipleUpdates() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            StockEntity stock = new StockEntity();
            stock.setTicker("NFLX");
            stock.setStrategyId(40L);
            entity.setStock(stock);

            // Act - First update
            entity.setCompliant(false);

            // Act - Second update
            entity.setCompliant(true);
            entity.setComplianceRate(BigDecimal.valueOf(75.00));
            entity.setSummary("Updated after re-evaluation");

            // Assert
            assertThat(entity.getStock().getTicker()).isEqualTo("NFLX");
            assertThat(entity.getStock().getStrategyId()).isEqualTo(40L);
            assertThat(entity.isCompliant()).isTrue();
            assertThat(entity.getComplianceRate()).isEqualByComparingTo(BigDecimal.valueOf(75.00));
            assertThat(entity.getSummary()).isEqualTo("Updated after re-evaluation");
        }

        @Test
        @DisplayName("Should handle updating latest flag multiple times")
        void shouldHandleUpdatingLatestFlagMultipleTimes() {
            // Arrange
            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();

            // Act
            entity.setLatest(true);
            assertThat(entity.isLatest()).isTrue();

            entity.setLatest(false);
            assertThat(entity.isLatest()).isFalse();

            entity.setLatest(true);
            assertThat(entity.isLatest()).isTrue();

            // Assert
            assertThat(entity.isLatest()).isTrue();
        }
    }
}
