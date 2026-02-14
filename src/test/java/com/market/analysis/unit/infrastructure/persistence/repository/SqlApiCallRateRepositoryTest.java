package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.ApiCallLog;
import com.market.analysis.infrastructure.persistence.entity.ApiCallLogEntity;
import com.market.analysis.infrastructure.persistence.mapper.ApiCallLogMapper;
import com.market.analysis.infrastructure.persistence.repository.JpaApiCallRateRepository;
import com.market.analysis.infrastructure.persistence.repository.SqlApiCallRateRepository;

/**
 * Unit tests for SqlApiCallRateRepository.
 * Tests persistence operations for API call logging.
 */
@DisplayName("SqlApiCallRateRepository Unit Tests")
@ExtendWith(MockitoExtension.class)
class SqlApiCallRateRepositoryTest {

    @Mock
    private JpaApiCallRateRepository jpaRepository;

    @Mock
    private ApiCallLogMapper mapper;

    @InjectMocks
    private SqlApiCallRateRepository repository;

    private ApiCallLogEntity testEntity;
    private ApiCallLog testDomain;
    private Instant testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = Instant.parse("2026-02-14T10:00:00Z");

        testEntity = ApiCallLogEntity.builder()
                .id(1L)
                .ticker("AAPL")
                .ocurredAt(testTimestamp)
                .build();

        testDomain = ApiCallLog.builder()
                .id(1L)
                .ticker("AAPL")
                .ocurredAt(testTimestamp)
                .build();
    }

    @Test
    @DisplayName("Should find API call log by ticker")
    void testFindByTicker() {
        // Arrange
        String ticker = "AAPL";
        when(jpaRepository.findByTicker(ticker)).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        // Act
        Optional<ApiCallLog> result = repository.findByTicker(ticker);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTicker()).isEqualTo(ticker);
        assertThat(result.get().getOcurredAt()).isEqualTo(testTimestamp);
        verify(jpaRepository, times(1)).findByTicker(ticker);
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    @DisplayName("Should return empty optional when ticker not found")
    void testFindByTickerNotFound() {
        // Arrange
        String ticker = "UNKNOWN";
        when(jpaRepository.findByTicker(ticker)).thenReturn(null);
        when(mapper.toDomain(null)).thenReturn(null);

        // Act
        Optional<ApiCallLog> result = repository.findByTicker(ticker);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository, times(1)).findByTicker(ticker);
        verify(mapper, times(1)).toDomain(null);
    }

    @Test
    @DisplayName("Should save API call log with ticker and timestamp")
    void testSave() {
        // Arrange
        String ticker = "AAPL";
        Instant timestamp = Instant.parse("2026-02-14T11:00:00Z");
        String timestampString = timestamp.toString();

        ApiCallLogEntity entityToSave = ApiCallLogEntity.builder()
                .ticker(ticker)
                .ocurredAt(timestamp)
                .build();

        when(mapper.toEntity(ticker, timestampString)).thenReturn(entityToSave);
        when(jpaRepository.save(entityToSave)).thenReturn(entityToSave);

        // Act
        repository.save(ticker, timestamp);

        // Assert
        verify(mapper, times(1)).toEntity(ticker, timestampString);
        verify(jpaRepository, times(1)).save(entityToSave);
    }

    @Test
    @DisplayName("Should delete API call log by ticker")
    void testDeleteByTicker() {
        // Arrange
        String ticker = "AAPL";

        // Act
        repository.deleteByTicker(ticker);

        // Assert
        verify(jpaRepository, times(1)).deleteByTicker(ticker);
    }

    @Test
    @DisplayName("Should handle save with different ticker formats")
    void testSaveWithDifferentTickerFormats() {
        // Arrange
        String ticker1 = "aapl";
        String ticker2 = "BRK.B";
        Instant timestamp = Instant.now();
        String timestampString = timestamp.toString();

        ApiCallLogEntity entity1 = ApiCallLogEntity.builder()
                .ticker(ticker1)
                .ocurredAt(timestamp)
                .build();

        ApiCallLogEntity entity2 = ApiCallLogEntity.builder()
                .ticker(ticker2)
                .ocurredAt(timestamp)
                .build();

        when(mapper.toEntity(ticker1, timestampString)).thenReturn(entity1);
        when(mapper.toEntity(ticker2, timestampString)).thenReturn(entity2);

        // Act
        repository.save(ticker1, timestamp);
        repository.save(ticker2, timestamp);

        // Assert
        verify(mapper, times(1)).toEntity(ticker1, timestampString);
        verify(mapper, times(1)).toEntity(ticker2, timestampString);
        verify(jpaRepository, times(1)).save(entity1);
        verify(jpaRepository, times(1)).save(entity2);
    }

    @Test
    @DisplayName("Should preserve timestamp precision when saving")
    void testSavePreservesTimestampPrecision() {
        // Arrange
        String ticker = "AAPL";
        Instant timestamp = Instant.parse("2026-02-14T10:15:30.123456789Z");
        String timestampString = timestamp.toString();

        ApiCallLogEntity entityToSave = ApiCallLogEntity.builder()
                .ticker(ticker)
                .ocurredAt(timestamp)
                .build();

        when(mapper.toEntity(ticker, timestampString)).thenReturn(entityToSave);
        when(jpaRepository.save(entityToSave)).thenReturn(entityToSave);

        // Act
        repository.save(ticker, timestamp);

        // Assert
        verify(mapper, times(1)).toEntity(ticker, timestampString);
        verify(jpaRepository, times(1)).save(entityToSave);
    }

    @Test
    @DisplayName("Should handle multiple saves for same ticker")
    void testMultipleSavesForSameTicker() {
        // Arrange
        String ticker = "AAPL";
        Instant timestamp1 = Instant.parse("2026-02-14T10:00:00Z");
        Instant timestamp2 = Instant.parse("2026-02-14T11:00:00Z");

        ApiCallLogEntity entity1 = ApiCallLogEntity.builder()
                .ticker(ticker)
                .ocurredAt(timestamp1)
                .build();

        ApiCallLogEntity entity2 = ApiCallLogEntity.builder()
                .ticker(ticker)
                .ocurredAt(timestamp2)
                .build();

        when(mapper.toEntity(ticker, timestamp1.toString())).thenReturn(entity1);
        when(mapper.toEntity(ticker, timestamp2.toString())).thenReturn(entity2);

        // Act
        repository.save(ticker, timestamp1);
        repository.save(ticker, timestamp2);

        // Assert
        verify(jpaRepository, times(1)).save(entity1);
        verify(jpaRepository, times(1)).save(entity2);
    }

    @Test
    @DisplayName("Should handle delete for non-existing ticker")
    void testDeleteNonExistingTicker() {
        // Arrange
        String ticker = "UNKNOWN";

        // Act
        repository.deleteByTicker(ticker);

        // Assert
        verify(jpaRepository, times(1)).deleteByTicker(ticker);
    }
}
