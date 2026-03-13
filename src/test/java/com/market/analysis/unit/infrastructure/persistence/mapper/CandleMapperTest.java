package com.market.analysis.unit.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Candle;
import com.market.analysis.infrastructure.persistence.entity.CandleEntity;
import com.market.analysis.infrastructure.persistence.mapper.CandleMapper;

/**
 * Unit tests for CandleMapper.
 */
@DisplayName("CandleMapper Unit Tests")
class CandleMapperTest {

    private CandleMapper candleMapper;

    @BeforeEach
    void setUp() {
        candleMapper = new CandleMapper();
    }

    @Test
    @DisplayName("Should map Candle domain to CandleEntity")
    void testToEntity() {
        // Arrange
        Instant dateTime = Instant.now();
        Candle candle = Candle.builder()
                .dateTime(dateTime)
                .openPrice(new BigDecimal("100.50"))
                .highPrice(new BigDecimal("102.00"))
                .lowPrice(new BigDecimal("99.75"))
                .closePrice(new BigDecimal("101.25"))
                .volume(1000000L)
                .build();

        // Act
        CandleEntity entity = candleMapper.toEntity(candle);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getDateTime()).isEqualTo(dateTime);
        assertThat(entity.getOpenPrice()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(entity.getHighPrice()).isEqualByComparingTo(new BigDecimal("102.00"));
        assertThat(entity.getLowPrice()).isEqualByComparingTo(new BigDecimal("99.75"));
        assertThat(entity.getClosePrice()).isEqualByComparingTo(new BigDecimal("101.25"));
        assertThat(entity.getVolume()).isEqualTo(1000000L);
    }

    @Test
    @DisplayName("Should map CandleEntity to Candle domain")
    void testToDomain() {
        // Arrange
        Instant dateTime = Instant.now();
        CandleEntity entity = new CandleEntity();
        entity.setId(1L);
        entity.setDateTime(dateTime);
        entity.setOpenPrice(new BigDecimal("100.50"));
        entity.setHighPrice(new BigDecimal("102.00"));
        entity.setLowPrice(new BigDecimal("99.75"));
        entity.setClosePrice(new BigDecimal("101.25"));
        entity.setVolume(1000000L);

        // Act
        Candle candle = candleMapper.toDomain(entity);

        // Assert
        assertThat(candle).isNotNull();
        assertThat(candle.getDateTime()).isEqualTo(dateTime);
        assertThat(candle.getOpenPrice()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(candle.getHighPrice()).isEqualByComparingTo(new BigDecimal("102.00"));
        assertThat(candle.getLowPrice()).isEqualByComparingTo(new BigDecimal("99.75"));
        assertThat(candle.getClosePrice()).isEqualByComparingTo(new BigDecimal("101.25"));
        assertThat(candle.getVolume()).isEqualTo(1000000L);
    }

    @Test
    @DisplayName("Should return null when mapping null Candle to entity")
    void testToEntityWithNull() {
        // Act
        CandleEntity entity = candleMapper.toEntity(null);

        // Assert
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should return null when mapping null CandleEntity to domain")
    void testToDomainWithNull() {
        // Act
        Candle candle = candleMapper.toDomain(null);

        // Assert
        assertThat(candle).isNull();
    }

    @Test
    @DisplayName("Should correctly map candle with zero values")
    void testToEntityWithZeroValues() {
        // Arrange
        Instant dateTime = Instant.now();
        Candle candle = Candle.builder()
                .dateTime(dateTime)
                .openPrice(BigDecimal.ZERO)
                .highPrice(BigDecimal.ZERO)
                .lowPrice(BigDecimal.ZERO)
                .closePrice(BigDecimal.ZERO)
                .volume(0L)
                .build();

        // Act
        CandleEntity entity = candleMapper.toEntity(candle);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getOpenPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entity.getVolume()).isZero();
    }
}
