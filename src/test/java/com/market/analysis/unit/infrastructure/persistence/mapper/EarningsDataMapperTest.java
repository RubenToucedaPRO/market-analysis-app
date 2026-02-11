package com.market.analysis.unit.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.EarningsData;
import com.market.analysis.infrastructure.persistence.entity.EarningsDataEntity;
import com.market.analysis.infrastructure.persistence.mapper.EarningsDataMapper;

/**
 * Unit tests for EarningsDataMapper.
 */
@DisplayName("EarningsDataMapper Unit Tests")
class EarningsDataMapperTest {

    private EarningsDataMapper earningsDataMapper;

    @BeforeEach
    void setUp() {
        earningsDataMapper = new EarningsDataMapper();
    }

    @Test
    @DisplayName("Should map EarningsData domain to EarningsDataEntity")
    void testToEntity() {
        // Arrange
        EarningsData earningsData = EarningsData.builder()
                .ticker("AAPL")
                .date("2024-06-15")
                .build();

        // Act
        EarningsDataEntity entity = earningsDataMapper.toEntity(earningsData);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getTicker()).isEqualTo("AAPL");
        assertThat(entity.getDate()).isEqualTo("2024-06-15");
    }

    @Test
    @DisplayName("Should map EarningsDataEntity to EarningsData domain")
    void testToDomain() {
        // Arrange
        EarningsDataEntity entity = new EarningsDataEntity();
        entity.setTicker("GOOGL");
        entity.setDate("2024-06-15");

        // Act
        EarningsData earningsData = earningsDataMapper.toDomain(entity);

        // Assert
        assertThat(earningsData).isNotNull();
        assertThat(earningsData.getTicker()).isEqualTo("GOOGL");
        assertThat(earningsData.getDate()).isEqualTo("2024-06-15");
    }

    @Test
    @DisplayName("Should return null when mapping null EarningsData to entity")
    void testToEntityWithNull() {
        // Act
        EarningsDataEntity entity = earningsDataMapper.toEntity(null);

        // Assert
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should return null when mapping null EarningsDataEntity to domain")
    void testToDomainWithNull() {
        // Act
        EarningsData earningsData = earningsDataMapper.toDomain(null);

        // Assert
        assertThat(earningsData).isNull();
    }

    @Test
    @DisplayName("Should correctly map earnings data with dot in ticker symbol")
    void testToEntityWithDotInTicker() {
        // Arrange
        EarningsData earningsData = EarningsData.builder()
                .ticker("BRK.B")
                .date("2024-01-01")
                .build();

        // Act
        EarningsDataEntity entity = earningsDataMapper.toEntity(earningsData);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getTicker()).isEqualTo("BRK.B");
        assertThat(entity.getDate()).isEqualTo("2024-01-01");
    }
}
