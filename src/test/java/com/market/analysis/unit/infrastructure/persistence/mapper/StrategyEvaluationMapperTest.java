package com.market.analysis.unit.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.infrastructure.persistence.entity.StockEntity;
import com.market.analysis.infrastructure.persistence.entity.StrategyEvaluationEntity;
import com.market.analysis.infrastructure.persistence.mapper.StrategyEvaluationMapper;

/**
 * Unit tests for StrategyEvaluationMapper.
 * Validates bidirectional mapping between domain model and JPA entity.
 */
@DisplayName("StrategyEvaluationMapper Unit Tests")
class StrategyEvaluationMapperTest {

    private StrategyEvaluationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new StrategyEvaluationMapper();
    }

    @Nested
    @DisplayName("Domain to Entity Mapping Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should convert domain model to entity with all fields")
        void shouldConvertDomainToEntityWithAllFields() {
            // Arrange
            LocalDateTime evaluatedAt = LocalDateTime.of(2026, 2, 12, 10, 30, 0);
            StockEntity stock = new StockEntity();
            stock.setId(1L);
            stock.setTicker("AAPL");
            stock.setStrategyId(10L);

            StrategyEvaluation domain = StrategyEvaluation.builder()
                    .id(1L)
                    .ticker("AAPL")
                    .strategyId(10L)
                    .compliant(true)
                    .complianceRate(BigDecimal.valueOf(85.50))
                    .summary("Strategy passed with 85.50% compliance")
                    .evaluatedAt(evaluatedAt)
                    .priceAtEvaluation(BigDecimal.valueOf(150.25))
                    .isLatest(true)
                    .build();

            // Act
            StrategyEvaluationEntity entity = mapper.toEntity(domain, stock);

            // Assert
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getStock()).isEqualTo(stock);
            assertThat(entity.isCompliant()).isTrue();
            assertThat(entity.getComplianceRate()).isEqualByComparingTo(BigDecimal.valueOf(85.50));
            assertThat(entity.getSummary()).isEqualTo("Strategy passed with 85.50% compliance");
            assertThat(entity.getEvaluatedAt()).isEqualTo(evaluatedAt);
            assertThat(entity.getPriceAtEvaluation()).isEqualByComparingTo(BigDecimal.valueOf(150.25));
            assertThat(entity.isLatest()).isTrue();
        }

        @Test
        @DisplayName("Should convert failed evaluation to entity")
        void shouldConvertFailedEvaluationToEntity() {
            // Arrange
            StockEntity stock = new StockEntity();
            stock.setId(2L);
            stock.setTicker("GOOGL");
            stock.setStrategyId(5L);

            StrategyEvaluation domain = StrategyEvaluation.builder()
                    .id(2L)
                    .ticker("GOOGL")
                    .strategyId(5L)
                    .compliant(false)
                    .complianceRate(BigDecimal.valueOf(45.00))
                    .summary("Strategy failed - insufficient compliance")
                    .evaluatedAt(LocalDateTime.now())
                    .priceAtEvaluation(BigDecimal.valueOf(2800.00))
                    .isLatest(false)
                    .build();

            // Act
            StrategyEvaluationEntity entity = mapper.toEntity(domain, stock);

            // Assert
            assertThat(entity).isNotNull();
            assertThat(entity.isCompliant()).isFalse();
            assertThat(entity.getComplianceRate()).isEqualByComparingTo(BigDecimal.valueOf(45.00));
            assertThat(entity.isLatest()).isFalse();
        }

        @Test
        @DisplayName("Should return null when domain is null")
        void shouldReturnNullWhenDomainIsNull() {
            // Arrange
            StockEntity stock = new StockEntity();
            stock.setId(1L);

            // Act
            StrategyEvaluationEntity entity = mapper.toEntity(null, stock);

            // Assert
            assertThat(entity).isNull();
        }

        @Test
        @DisplayName("Should handle domain with null optional fields")
        void shouldHandleDomainWithNullOptionalFields() {
            // Arrange
            StockEntity stock = new StockEntity();
            stock.setId(3L);
            stock.setTicker("TSLA");
            stock.setStrategyId(3L);

            StrategyEvaluation domain = StrategyEvaluation.builder()
                    .ticker("TSLA")
                    .strategyId(3L)
                    .compliant(true)
                    .complianceRate(BigDecimal.valueOf(100.00))
                    .evaluatedAt(LocalDateTime.now())
                    .isLatest(true)
                    // id, summary, priceAtEvaluation are null
                    .build();

            // Act
            StrategyEvaluationEntity entity = mapper.toEntity(domain, stock);

            // Assert
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isNull();
            assertThat(entity.getSummary()).isNull();
            assertThat(entity.getPriceAtEvaluation()).isNull();
            assertThat(entity.getStock()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Entity to Domain Mapping Tests")
    class ToDomainTests {

        @Test
        @DisplayName("Should convert entity to domain model with all fields")
        void shouldConvertEntityToDomainWithAllFields() {
            // Arrange
            LocalDateTime evaluatedAt = LocalDateTime.of(2026, 2, 12, 14, 45, 30);
            StockEntity stock = new StockEntity();
            stock.setId(100L);
            stock.setTicker("MSFT");
            stock.setStrategyId(20L);

            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            entity.setId(100L);
            entity.setStock(stock);
            entity.setCompliant(true);
            entity.setComplianceRate(BigDecimal.valueOf(92.75));
            entity.setSummary("Excellent strategy performance");
            entity.setEvaluatedAt(evaluatedAt);
            entity.setPriceAtEvaluation(BigDecimal.valueOf(380.50));
            entity.setLatest(true);

            // Act
            StrategyEvaluation domain = mapper.toDomain(entity);

            // Assert
            assertThat(domain).isNotNull();
            assertThat(domain.getId()).isEqualTo(100L);
            assertThat(domain.getTicker()).isEqualTo("MSFT");
            assertThat(domain.getStrategyId()).isEqualTo(20L);
            assertThat(domain.isCompliant()).isTrue();
            assertThat(domain.getComplianceRate()).isEqualByComparingTo(BigDecimal.valueOf(92.75));
            assertThat(domain.getSummary()).isEqualTo("Excellent strategy performance");
            assertThat(domain.getEvaluatedAt()).isEqualTo(evaluatedAt);
            assertThat(domain.getPriceAtEvaluation()).isEqualByComparingTo(BigDecimal.valueOf(380.50));
            assertThat(domain.isLatest()).isTrue();
        }

        @Test
        @DisplayName("Should convert failed evaluation entity to domain")
        void shouldConvertFailedEvaluationEntityToDomain() {
            // Arrange
            StockEntity stock = new StockEntity();
            stock.setId(200L);
            stock.setTicker("AMZN");
            stock.setStrategyId(15L);

            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            entity.setId(200L);
            entity.setStock(stock);
            entity.setCompliant(false);
            entity.setComplianceRate(BigDecimal.valueOf(30.00));
            entity.setSummary("Strategy failed multiple rules");
            entity.setEvaluatedAt(LocalDateTime.now());
            entity.setPriceAtEvaluation(BigDecimal.valueOf(3200.00));
            entity.setLatest(false);

            // Act
            StrategyEvaluation domain = mapper.toDomain(entity);

            // Assert
            assertThat(domain).isNotNull();
            assertThat(domain.isCompliant()).isFalse();
            assertThat(domain.getComplianceRate()).isEqualByComparingTo(BigDecimal.valueOf(30.00));
            assertThat(domain.isLatest()).isFalse();
        }

        @Test
        @DisplayName("Should return null when entity is null")
        void shouldReturnNullWhenEntityIsNull() {
            // Act
            StrategyEvaluation domain = mapper.toDomain(null);

            // Assert
            assertThat(domain).isNull();
        }

        @Test
        @DisplayName("Should handle entity with null optional fields")
        void shouldHandleEntityWithNullOptionalFields() {
            // Arrange
            StockEntity stock = new StockEntity();
            stock.setId(7L);
            stock.setTicker("NVDA");
            stock.setStrategyId(7L);

            StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
            entity.setStock(stock);
            entity.setCompliant(true);
            entity.setComplianceRate(BigDecimal.valueOf(88.00));
            entity.setEvaluatedAt(LocalDateTime.now());
            entity.setLatest(true);
            // id, summary, priceAtEvaluation are null

            // Act
            StrategyEvaluation domain = mapper.toDomain(entity);

            // Assert
            assertThat(domain).isNotNull();
            assertThat(domain.getId()).isNull();
            assertThat(domain.getSummary()).isNull();
            assertThat(domain.getPriceAtEvaluation()).isNull();
            assertThat(domain.getTicker()).isEqualTo("NVDA");
        }
    }

    @Nested
    @DisplayName("Bidirectional Mapping Tests")
    class BidirectionalTests {

        @Test
        @DisplayName("Should preserve data in round-trip domain -> entity -> domain")
        void shouldPreserveDataInRoundTripDomainToEntityToDomain() {
            // Arrange
            LocalDateTime evaluatedAt = LocalDateTime.of(2026, 2, 12, 12, 0, 0);
            StockEntity stock = new StockEntity();
            stock.setId(999L);
            stock.setTicker("META");
            stock.setStrategyId(50L);

            StrategyEvaluation originalDomain = StrategyEvaluation.builder()
                    .id(999L)
                    .ticker("META")
                    .strategyId(50L)
                    .compliant(true)
                    .complianceRate(BigDecimal.valueOf(78.33))
                    .summary("Good compliance rate achieved")
                    .evaluatedAt(evaluatedAt)
                    .priceAtEvaluation(BigDecimal.valueOf(450.00))
                    .isLatest(true)
                    .build();

            // Act
            StrategyEvaluationEntity entity = mapper.toEntity(originalDomain, stock);
            StrategyEvaluation resultDomain = mapper.toDomain(entity);

            // Assert
            assertThat(resultDomain).isNotNull();
            assertThat(resultDomain.getId()).isEqualTo(originalDomain.getId());
            assertThat(resultDomain.getTicker()).isEqualTo(originalDomain.getTicker());
            assertThat(resultDomain.getStrategyId()).isEqualTo(originalDomain.getStrategyId());
            assertThat(resultDomain.isCompliant()).isEqualTo(originalDomain.isCompliant());
            assertThat(resultDomain.getComplianceRate()).isEqualByComparingTo(originalDomain.getComplianceRate());
            assertThat(resultDomain.getSummary()).isEqualTo(originalDomain.getSummary());
            assertThat(resultDomain.getEvaluatedAt()).isEqualTo(originalDomain.getEvaluatedAt());
            assertThat(resultDomain.getPriceAtEvaluation())
                    .isEqualByComparingTo(originalDomain.getPriceAtEvaluation());
            assertThat(resultDomain.isLatest()).isEqualTo(originalDomain.isLatest());
        }

        @Test
        @DisplayName("Should preserve data in round-trip entity -> domain -> entity")
        void shouldPreserveDataInRoundTripEntityToDomainToEntity() {
            // Arrange
            LocalDateTime evaluatedAt = LocalDateTime.of(2026, 2, 12, 16, 30, 0);
            StockEntity originalStock = new StockEntity();
            originalStock.setId(888L);
            originalStock.setTicker("NFLX");
            originalStock.setStrategyId(25L);

            StrategyEvaluationEntity originalEntity = new StrategyEvaluationEntity();
            originalEntity.setId(888L);
            originalEntity.setStock(originalStock);
            originalEntity.setCompliant(false);
            originalEntity.setComplianceRate(BigDecimal.valueOf(55.55));
            originalEntity.setSummary("Partial compliance only");
            originalEntity.setEvaluatedAt(evaluatedAt);
            originalEntity.setPriceAtEvaluation(BigDecimal.valueOf(600.00));
            originalEntity.setLatest(false);

            // Act
            StrategyEvaluation domain = mapper.toDomain(originalEntity);
            StrategyEvaluationEntity resultEntity = mapper.toEntity(domain, originalStock);

            // Assert
            assertThat(resultEntity).isNotNull();
            assertThat(resultEntity.getId()).isEqualTo(originalEntity.getId());
            assertThat(resultEntity.getStock()).isEqualTo(originalStock);
            assertThat(resultEntity.isCompliant()).isEqualTo(originalEntity.isCompliant());
            assertThat(resultEntity.getComplianceRate())
                    .isEqualByComparingTo(originalEntity.getComplianceRate());
            assertThat(resultEntity.getSummary()).isEqualTo(originalEntity.getSummary());
            assertThat(resultEntity.getEvaluatedAt()).isEqualTo(originalEntity.getEvaluatedAt());
            assertThat(resultEntity.getPriceAtEvaluation())
                    .isEqualByComparingTo(originalEntity.getPriceAtEvaluation());
            assertThat(resultEntity.isLatest()).isEqualTo(originalEntity.isLatest());
        }
    }
}
