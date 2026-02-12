package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.infrastructure.persistence.entity.StrategyEvaluationEntity;
import com.market.analysis.infrastructure.persistence.mapper.StrategyEvaluationMapper;
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
    private StrategyEvaluationMapper mapper;

    @InjectMocks
    private SqlStrategyEvaluationRepository repository;

    private StrategyEvaluation testDomainEvaluation;
    private StrategyEvaluationEntity testEntity;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

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
        testEntity.setTicker("AAPL");
        testEntity.setStrategyId(10L);
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
        @DisplayName("Should save evaluation and return domain model")
        void shouldSaveEvaluationAndReturnDomainModel() {
            // Arrange
            when(mapper.toEntity(testDomainEvaluation)).thenReturn(testEntity);
            when(jpaRepository.save(testEntity)).thenReturn(testEntity);
            when(mapper.toDomain(testEntity)).thenReturn(testDomainEvaluation);

            // Act
            StrategyEvaluation result = repository.save(testDomainEvaluation);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTicker()).isEqualTo("AAPL");
            verify(mapper).toEntity(testDomainEvaluation);
            verify(jpaRepository).save(testEntity);
            verify(mapper).toDomain(testEntity);
        }

        @Test
        @DisplayName("Should mark as latest when isLatest is true")
        void shouldMarkAsLatestWhenIsLatestIsTrue() {
            // Arrange
            when(mapper.toEntity(testDomainEvaluation)).thenReturn(testEntity);
            when(jpaRepository.save(testEntity)).thenReturn(testEntity);
            when(mapper.toDomain(testEntity)).thenReturn(testDomainEvaluation);

            // Act
            repository.save(testDomainEvaluation);

            // Assert
            verify(jpaRepository).updateLatestToFalse(
                    "AAPL",
                    10L,
                    1L);
        }

        @Test
        @DisplayName("Should not mark as latest when isLatest is false")
        void shouldNotMarkAsLatestWhenIsLatestIsFalse() {
            // Arrange
            testEntity.setLatest(false);
            StrategyEvaluation notLatestEvaluation = StrategyEvaluation.builder()
                    .id(2L)
                    .ticker("GOOGL")
                    .strategyId(5L)
                    .compliant(true)
                    .complianceRate(BigDecimal.valueOf(90.00))
                    .evaluatedAt(LocalDateTime.now())
                    .isLatest(false)
                    .build();

            when(mapper.toEntity(notLatestEvaluation)).thenReturn(testEntity);
            when(jpaRepository.save(testEntity)).thenReturn(testEntity);
            when(mapper.toDomain(testEntity)).thenReturn(notLatestEvaluation);

            // Act
            repository.save(notLatestEvaluation);

            // Assert
            verify(jpaRepository, times(0)).updateLatestToFalse(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Find Operations Tests")
    class FindTests {

        @Test
        @DisplayName("Should find latest evaluation by ticker and strategy ID")
        void shouldFindLatestEvaluationByTickerAndStrategyId() {
            // Arrange
            when(jpaRepository.findFirstByTickerAndStrategyIdAndLatestTrueOrderByEvaluatedAtDesc(
                    "AAPL", 10L))
                    .thenReturn(Optional.of(testEntity));
            when(mapper.toDomain(testEntity)).thenReturn(testDomainEvaluation);

            // Act
            Optional<StrategyEvaluation> result = repository.findLatestByTickerAndStrategyId("AAPL", 10L);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getTicker()).isEqualTo("AAPL");
            assertThat(result.get().getStrategyId()).isEqualTo(10L);
            assertThat(result.get().isLatest()).isTrue();
        }

        @Test
        @DisplayName("Should return empty when no latest evaluation found")
        void shouldReturnEmptyWhenNoLatestEvaluationFound() {
            // Arrange
            when(jpaRepository.findFirstByTickerAndStrategyIdAndLatestTrueOrderByEvaluatedAtDesc(
                    "UNKNOWN", 999L))
                    .thenReturn(Optional.empty());

            // Act
            Optional<StrategyEvaluation> result = repository.findLatestByTickerAndStrategyId("UNKNOWN", 999L);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all evaluations by ticker")
        void shouldFindAllEvaluationsByTicker() {
            // Arrange
            StrategyEvaluationEntity entity2 = new StrategyEvaluationEntity();
            entity2.setId(2L);
            entity2.setTicker("AAPL");
            entity2.setStrategyId(5L);

            StrategyEvaluation domain2 = StrategyEvaluation.builder()
                    .id(2L)
                    .ticker("AAPL")
                    .strategyId(5L)
                    .build();

            when(jpaRepository.findByTickerOrderByEvaluatedAtDesc("AAPL"))
                    .thenReturn(Arrays.asList(testEntity, entity2));
            when(mapper.toDomain(testEntity)).thenReturn(testDomainEvaluation);
            when(mapper.toDomain(entity2)).thenReturn(domain2);

            // Act
            List<StrategyEvaluation> result = repository.findByTicker("AAPL");

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should return empty list when no evaluations found by ticker")
        void shouldReturnEmptyListWhenNoEvaluationsFoundByTicker() {
            // Arrange
            when(jpaRepository.findByTickerOrderByEvaluatedAtDesc("UNKNOWN"))
                    .thenReturn(Arrays.asList());

            // Act
            List<StrategyEvaluation> result = repository.findByTicker("UNKNOWN");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all evaluations by strategy ID")
        void shouldFindAllEvaluationsByStrategyId() {
            // Arrange
            StrategyEvaluationEntity entity2 = new StrategyEvaluationEntity();
            entity2.setId(3L);
            entity2.setTicker("GOOGL");
            entity2.setStrategyId(10L);

            StrategyEvaluation domain2 = StrategyEvaluation.builder()
                    .id(3L)
                    .ticker("GOOGL")
                    .strategyId(10L)
                    .build();

            when(jpaRepository.findByStrategyIdOrderByEvaluatedAtDesc(10L))
                    .thenReturn(Arrays.asList(testEntity, entity2));
            when(mapper.toDomain(testEntity)).thenReturn(testDomainEvaluation);
            when(mapper.toDomain(entity2)).thenReturn(domain2);

            // Act
            List<StrategyEvaluation> result = repository.findByStrategyId(10L);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStrategyId()).isEqualTo(10L);
            assertThat(result.get(1).getStrategyId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should return empty list when no evaluations found by strategy ID")
        void shouldReturnEmptyListWhenNoEvaluationsFoundByStrategyId() {
            // Arrange
            when(jpaRepository.findByStrategyIdOrderByEvaluatedAtDesc(999L))
                    .thenReturn(Arrays.asList());

            // Act
            List<StrategyEvaluation> result = repository.findByStrategyId(999L);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find evaluation by ID")
        void shouldFindEvaluationById() {
            // Arrange
            when(jpaRepository.findById(1L)).thenReturn(Optional.of(testEntity));
            when(mapper.toDomain(testEntity)).thenReturn(testDomainEvaluation);

            // Act
            Optional<StrategyEvaluation> result = repository.findById(1L);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return empty when evaluation not found by ID")
        void shouldReturnEmptyWhenEvaluationNotFoundById() {
            // Arrange
            when(jpaRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<StrategyEvaluation> result = repository.findById(999L);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Delete Operation Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete evaluation by ID")
        void shouldDeleteEvaluationById() {
            // Act
            repository.deleteById(1L);

            // Assert
            verify(jpaRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should call deleteById even if evaluation does not exist")
        void shouldCallDeleteByIdEvenIfEvaluationDoesNotExist() {
            // Act
            repository.deleteById(999L);

            // Assert
            verify(jpaRepository).deleteById(999L);
        }
    }

    @Nested
    @DisplayName("Mark As Latest Tests")
    class MarkAsLatestTests {

        @Test
        @DisplayName("Should mark evaluation as latest for ticker and strategy")
        void shouldMarkEvaluationAsLatestForTickerAndStrategy() {
            // Act
            repository.markAsLatestForTickerAndStrategy(1L, "AAPL", 10L);

            // Assert
            verify(jpaRepository).updateLatestToFalse("AAPL", 10L, 1L);
        }

        @Test
        @DisplayName("Should call updateLatestToFalse with correct parameters")
        void shouldCallUpdateLatestToFalseWithCorrectParameters() {
            // Act
            repository.markAsLatestForTickerAndStrategy(100L, "MSFT", 50L);

            // Assert
            verify(jpaRepository).updateLatestToFalse(
                    "MSFT",
                    50L,
                    100L);
        }
    }
}
