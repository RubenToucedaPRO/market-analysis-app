package com.market.analysis.unit.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.ApiCallLog;
import com.market.analysis.infrastructure.persistence.entity.ApiCallLogEntity;
import com.market.analysis.infrastructure.persistence.mapper.ApiCallLogMapper;

/**
 * Unit tests for ApiCallLogMapper.
 * Tests mapping between ApiCallLog domain model and ApiCallLogEntity.
 */
@DisplayName("ApiCallLogMapper Unit Tests")
class ApiCallLogMapperTest {

    private ApiCallLogMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ApiCallLogMapper();
    }

    @Test
    @DisplayName("Should map ticker and timestamp to ApiCallLogEntity")
    void testToEntity() {
        // Arrange
        String ticker = "AAPL";
        Instant timestamp = Instant.parse("2026-02-14T10:00:00Z");
        String timestampString = timestamp.toString();

        // Act
        ApiCallLogEntity entity = mapper.toEntity(ticker, timestampString);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getTicker()).isEqualTo(ticker);
        assertThat(entity.getOcurredAt()).isEqualTo(timestamp);
        assertThat(entity.getId()).isNull(); // ID not set until persisted
    }

    @Test
    @DisplayName("Should map ApiCallLogEntity to ApiCallLog domain")
    void testToDomain() {
        // Arrange
        Instant timestamp = Instant.parse("2026-02-14T10:00:00Z");
        ApiCallLogEntity entity = ApiCallLogEntity.builder()
                .id(1L)
                .ticker("AAPL")
                .ocurredAt(timestamp)
                .build();

        // Act
        ApiCallLog domain = mapper.toDomain(entity);

        // Assert
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getTicker()).isEqualTo("AAPL");
        assertThat(domain.getOcurredAt()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("Should return null when mapping null ticker to entity")
    void testToEntityWithNullTicker() {
        // Arrange
        String timestampString = Instant.now().toString();

        // Act
        ApiCallLogEntity entity = mapper.toEntity(null, timestampString);

        // Assert
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should return null when mapping null timestamp to entity")
    void testToEntityWithNullTimestamp() {
        // Arrange
        String ticker = "AAPL";

        // Act
        ApiCallLogEntity entity = mapper.toEntity(ticker, null);

        // Assert
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should return null when mapping both null values to entity")
    void testToEntityWithNullValues() {
        // Act
        ApiCallLogEntity entity = mapper.toEntity(null, null);

        // Assert
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should return null when mapping null entity to domain")
    void testToDomainWithNull() {
        // Act
        ApiCallLog domain = mapper.toDomain(null);

        // Assert
        assertThat(domain).isNull();
    }

    @Test
    @DisplayName("Should correctly map entity without ID to domain")
    void testToDomainWithoutId() {
        // Arrange
        Instant timestamp = Instant.parse("2026-02-14T10:00:00Z");
        ApiCallLogEntity entity = ApiCallLogEntity.builder()
                .ticker("MSFT")
                .ocurredAt(timestamp)
                .build();

        // Act
        ApiCallLog domain = mapper.toDomain(entity);

        // Assert
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isNull();
        assertThat(domain.getTicker()).isEqualTo("MSFT");
        assertThat(domain.getOcurredAt()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("Should handle different ticker formats")
    void testToEntityWithDifferentTickerFormats() {
        // Arrange
        String timestamp = Instant.now().toString();

        // Act & Assert - lowercase ticker
        ApiCallLogEntity entity1 = mapper.toEntity("aapl", timestamp);
        assertThat(entity1).isNotNull();
        assertThat(entity1.getTicker()).isEqualTo("aapl");

        // Act & Assert - ticker with numbers
        ApiCallLogEntity entity2 = mapper.toEntity("BRK.B", timestamp);
        assertThat(entity2).isNotNull();
        assertThat(entity2.getTicker()).isEqualTo("BRK.B");
    }

    @Test
    @DisplayName("Should preserve exact timestamp precision")
    void testToEntityPreservesTimestampPrecision() {
        // Arrange
        String ticker = "AAPL";
        Instant timestamp = Instant.parse("2026-02-14T10:15:30.123456789Z");
        String timestampString = timestamp.toString();

        // Act
        ApiCallLogEntity entity = mapper.toEntity(ticker, timestampString);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getOcurredAt()).isEqualTo(timestamp);
        assertThat(entity.getOcurredAt().getNano()).isEqualTo(timestamp.getNano());
    }
}
