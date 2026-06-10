package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.infrastructure.persistence.entity.StockEntity;
import com.market.analysis.infrastructure.persistence.entity.StrategyEvaluationEntity;
import com.market.analysis.infrastructure.persistence.mapper.StrategyEvaluationMapper;
import com.market.analysis.infrastructure.persistence.repository.JpaStockDataRepository;
import com.market.analysis.infrastructure.persistence.repository.JpaStrategyEvaluationRepository;
import com.market.analysis.infrastructure.persistence.repository.SqlStrategyEvaluationRepository;

/**
 * Unit tests for SqlStrategyEvaluationRepository.
 * Validates repository operations with mocked JPA repository.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SqlStrategyEvaluationRepository Unit Tests")
class SqlStrategyEvaluationRepositoryTest {

    @Mock
    private JpaStrategyEvaluationRepository jpaRepository;

    @Mock
    private JpaStockDataRepository jpaStockRepository;

    @Mock
    private StrategyEvaluationMapper mapper;

    @InjectMocks
    private SqlStrategyEvaluationRepository repository;

    private StrategyEvaluation testDomainEvaluation;
    private StrategyEvaluationEntity testEntity;
    private StockEntity testStock;
    private Stock testDomainStock;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        // Setup Stock entities and domain
        testStock = new StockEntity();
        testStock.setId(1L);
        testStock.setTicker("AAPL");
        testStock.setStrategyId(10L);

        testDomainStock = Stock.builder()
                .id(1L)
                .ticker("AAPL")
                .strategyId(10L)
                .build();

        testDomainEvaluation = StrategyEvaluation.builder()
                .id(1L)
                .ticker("AAPL")
                .strategyId(10L)
                .compliant(true)
                .complianceRate(BigDecimal.valueOf(85.50))
                .summary("Strategy passed")
                .evaluatedAt(now)
                .priceAtEvaluation(BigDecimal.valueOf(150.00))
                .isLatest(true)
                .build();

        testEntity = new StrategyEvaluationEntity();
        testEntity.setId(1L);
        testEntity.setStock(testStock);
        testEntity.setCompliant(true);
        testEntity.setComplianceRate(BigDecimal.valueOf(85.50));
        testEntity.setSummary("Strategy passed");
        testEntity.setEvaluatedAt(now);
        testEntity.setPriceAtEvaluation(BigDecimal.valueOf(150.00));
        testEntity.setLatest(true);
    }

    @Nested
    @DisplayName("Save Operation Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save evaluation successfully")
        void shouldSaveEvaluationSuccessfully() {
            // Arrange
            when(jpaStockRepository.findById(1L)).thenReturn(Optional.of(testStock));
            when(mapper.toEntity(testDomainEvaluation, testStock)).thenReturn(testEntity);
            when(jpaRepository.save(testEntity)).thenReturn(testEntity);
            when(mapper.toDomain(testEntity)).thenReturn(testDomainEvaluation);

            // Act
            StrategyEvaluation result = repository.save(testDomainEvaluation, testDomainStock);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTicker()).isEqualTo("AAPL");
            assertThat(result.isCompliant()).isTrue();
            verify(jpaRepository).save(testEntity);
        }

        @Test
        @DisplayName("Should throw exception when stock not found")
        void shouldThrowExceptionWhenStockNotFound() {
            // Arrange
            Stock unknownStock = Stock.builder()
                    .id(999L)
                    .ticker("UNKNOWN")
                    .strategyId(99L)
                    .build();

            when(jpaStockRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            try {
                repository.save(testDomainEvaluation, unknownStock);
                org.junit.jupiter.api.Assertions.fail("Should have thrown EntityNotFoundException");
            } catch (jakarta.persistence.EntityNotFoundException e) {
                assertThat(e.getMessage()).contains("Stock not found");
            }
        }

        @Test
        @DisplayName("Should save evaluation with minimum valid data")
        void shouldSaveEvaluationWithMinimumValidData() {
            // Arrange
            StrategyEvaluation minimalEvaluation = StrategyEvaluation.builder()
                    .compliant(false)
                    .complianceRate(BigDecimal.ZERO)
                    .evaluatedAt(Instant.now())
                    .isLatest(false)
                    .build();

            StrategyEvaluationEntity minimalEntity = new StrategyEvaluationEntity();
            minimalEntity.setStock(testStock);
            minimalEntity.setCompliant(false);
            minimalEntity.setComplianceRate(BigDecimal.ZERO);
            minimalEntity.setEvaluatedAt(Instant.now());
            minimalEntity.setLatest(false);

            when(jpaStockRepository.findById(1L)).thenReturn(Optional.of(testStock));
            when(mapper.toEntity(minimalEvaluation, testStock)).thenReturn(minimalEntity);
            when(jpaRepository.save(minimalEntity)).thenReturn(minimalEntity);
            when(mapper.toDomain(minimalEntity)).thenReturn(minimalEvaluation);

            // Act
            StrategyEvaluation result = repository.save(minimalEvaluation, testDomainStock);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isCompliant()).isFalse();
            assertThat(result.getComplianceRate()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should verify save is called exactly once")
        void shouldVerifySaveIsCalledExactlyOnce() {
            // Arrange
            when(jpaStockRepository.findById(1L)).thenReturn(Optional.of(testStock));
            when(mapper.toEntity(testDomainEvaluation, testStock)).thenReturn(testEntity);
            when(jpaRepository.save(testEntity)).thenReturn(testEntity);
            when(mapper.toDomain(testEntity)).thenReturn(testDomainEvaluation);

            // Act
            repository.save(testDomainEvaluation, testDomainStock);

            // Assert
            verify(jpaRepository, times(1)).save(any(StrategyEvaluationEntity.class));
        }

        @Test
        @DisplayName("Should map entity to domain after saving")
        void shouldMapEntityToDomainAfterSaving() {
            // Arrange
            when(jpaStockRepository.findById(1L)).thenReturn(Optional.of(testStock));
            when(mapper.toEntity(testDomainEvaluation, testStock)).thenReturn(testEntity);
            when(jpaRepository.save(testEntity)).thenReturn(testEntity);
            when(mapper.toDomain(testEntity)).thenReturn(testDomainEvaluation);

            // Act
            repository.save(testDomainEvaluation, testDomainStock);

            // Assert
            verify(mapper, times(1)).toDomain(testEntity);
        }

        @Test
        @DisplayName("Should map domain to entity before saving")
        void shouldMapDomainToEntityBeforeSaving() {
            // Arrange
            when(jpaStockRepository.findById(1L)).thenReturn(Optional.of(testStock));
            when(mapper.toEntity(testDomainEvaluation, testStock)).thenReturn(testEntity);
            when(jpaRepository.save(testEntity)).thenReturn(testEntity);
            when(mapper.toDomain(testEntity)).thenReturn(testDomainEvaluation);

            // Act
            repository.save(testDomainEvaluation, testDomainStock);

            // Assert
            verify(mapper, times(1)).toEntity(testDomainEvaluation, testStock);
        }

        @Test
        @DisplayName("Should preserve evaluation data during save")
        void shouldPreserveEvaluationDataDuringSave() {
            // Arrange
            StrategyEvaluation originalEvaluation = StrategyEvaluation.builder()
                    .id(5L)
                    .ticker("GOOGL")
                    .strategyId(15L)
                    .compliant(true)
                    .complianceRate(BigDecimal.valueOf(95.99))
                    .summary("Excellent performance")
                    .evaluatedAt(Instant.now())
                    .priceAtEvaluation(BigDecimal.valueOf(2800.75))
                    .isLatest(true)
                    .build();

            StockEntity googleStock = new StockEntity();
            googleStock.setId(2L);
            googleStock.setTicker("GOOGL");
            googleStock.setStrategyId(15L);

            StrategyEvaluationEntity savedEntity = new StrategyEvaluationEntity();
            savedEntity.setId(5L);
            savedEntity.setStock(googleStock);
            savedEntity.setCompliant(true);
            savedEntity.setComplianceRate(BigDecimal.valueOf(95.99));
            savedEntity.setSummary("Excellent performance");
            savedEntity.setEvaluatedAt(originalEvaluation.getEvaluatedAt());
            savedEntity.setPriceAtEvaluation(BigDecimal.valueOf(2800.75));
            savedEntity.setLatest(true);

            Stock googleDomainStock = Stock.builder()
                    .id(2L)
                    .ticker("GOOGL")
                    .strategyId(15L)
                    .build();

            when(jpaStockRepository.findById(2L)).thenReturn(Optional.of(googleStock));
            when(mapper.toEntity(originalEvaluation, googleStock)).thenReturn(savedEntity);
            when(jpaRepository.save(savedEntity)).thenReturn(savedEntity);
            when(mapper.toDomain(savedEntity)).thenReturn(originalEvaluation);

            // Act
            StrategyEvaluation result = repository.save(originalEvaluation, googleDomainStock);

            // Assert
            assertThat(result.getId()).isEqualTo(originalEvaluation.getId());
            assertThat(result.getComplianceRate()).isEqualByComparingTo(originalEvaluation.getComplianceRate());
            assertThat(result.getSummary()).isEqualTo(originalEvaluation.getSummary());
            assertThat(result.getPriceAtEvaluation())
                    .isEqualByComparingTo(originalEvaluation.getPriceAtEvaluation());
        }
    }
}
