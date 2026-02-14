package com.market.analysis.unit.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.infrastructure.persistence.entity.CompanyProfileEntity;
import com.market.analysis.infrastructure.persistence.entity.StockEntity;
import com.market.analysis.infrastructure.persistence.mapper.StockMapper;
import com.market.analysis.infrastructure.persistence.mapper.StrategyEvaluationMapper;

/**
 * Unit tests for StockMapper.
 */
@DisplayName("StockMapper Unit Tests")
@ExtendWith(MockitoExtension.class)
class StockMapperTest {

    @Mock
    private StrategyEvaluationMapper strategyEvaluationMapper;

    private StockMapper stockMapper;

    @BeforeEach
    void setUp() {
        stockMapper = new StockMapper(strategyEvaluationMapper);
    }

    @Test
    @DisplayName("Should map Stock domain to StockEntity")
    void testToEntity() {
        // Arrange
        Instant lastUpdate = Instant.now();
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .logoUrl("https://example.com/logo.png")
                .currentPrice(new BigDecimal("150.50"))
                .openPrice(new BigDecimal("149.00"))
                .highOfDay(new BigDecimal("151.00"))
                .lowOfDay(new BigDecimal("148.50"))
                .previousClose(new BigDecimal("148.00"))
                .sma20(new BigDecimal("147.00"))
                .sma50(new BigDecimal("145.00"))
                .sma200(new BigDecimal("140.00"))
                .volume(50000000L)
                .averageVolume(45000000L)
                .lastUpdated(lastUpdate)
                .build();

        // Act
        StockEntity entity = stockMapper.toEntity(stock);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getTicker()).isEqualTo("AAPL");
        assertThat(entity.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("150.50"));
        assertThat(entity.getOpenPrice()).isEqualByComparingTo(new BigDecimal("149.00"));
        assertThat(entity.getHighOfDay()).isEqualByComparingTo(new BigDecimal("151.00"));
        assertThat(entity.getLowOfDay()).isEqualByComparingTo(new BigDecimal("148.50"));
        assertThat(entity.getPreviousClose()).isEqualByComparingTo(new BigDecimal("148.00"));
        assertThat(entity.getSma20()).isEqualByComparingTo(new BigDecimal("147.00"));
        assertThat(entity.getSma50()).isEqualByComparingTo(new BigDecimal("145.00"));
        assertThat(entity.getSma200()).isEqualByComparingTo(new BigDecimal("140.00"));
        assertThat(entity.getVolume()).isEqualTo(50000000L);
    }

    @Test
    @DisplayName("Should map StockEntity to Stock domain with company profile")
    void testToDomainWithCompanyProfile() {
        // Arrange
        Instant lastUpdate = Instant.now();

        CompanyProfileEntity companyProfile = CompanyProfileEntity.builder()
                .ticker("AAPL")
                .logo("https://example.com/logo.png")
                .build();

        StockEntity entity = new StockEntity();
        entity.setId(1L);
        entity.setTicker("AAPL");
        entity.setCurrentPrice(new BigDecimal("150.50"));
        entity.setOpenPrice(new BigDecimal("149.00"));
        entity.setHighOfDay(new BigDecimal("151.00"));
        entity.setLowOfDay(new BigDecimal("148.50"));
        entity.setPreviousClose(new BigDecimal("148.00"));
        entity.setSma20(new BigDecimal("147.00"));
        entity.setSma50(new BigDecimal("145.00"));
        entity.setSma200(new BigDecimal("140.00"));
        entity.setVolume(50000000L);
        entity.setAverageVolume(45000000L);
        entity.setLastUpdate(lastUpdate);
        entity.setCompanyProfile(companyProfile);

        // Act
        Stock stock = stockMapper.toDomain(entity);

        // Assert
        assertThat(stock).isNotNull();
        assertThat(stock.getTicker()).isEqualTo("AAPL");
        assertThat(stock.getLogoUrl()).isEqualTo("https://example.com/logo.png");
        assertThat(stock.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("150.50"));
        assertThat(stock.getOpenPrice()).isEqualByComparingTo(new BigDecimal("149.00"));
        assertThat(stock.getHighOfDay()).isEqualByComparingTo(new BigDecimal("151.00"));
        assertThat(stock.getLowOfDay()).isEqualByComparingTo(new BigDecimal("148.50"));
        assertThat(stock.getPreviousClose()).isEqualByComparingTo(new BigDecimal("148.00"));
        assertThat(stock.getSma20()).isEqualByComparingTo(new BigDecimal("147.00"));
        assertThat(stock.getSma50()).isEqualByComparingTo(new BigDecimal("145.00"));
        assertThat(stock.getSma200()).isEqualByComparingTo(new BigDecimal("140.00"));
        assertThat(stock.getVolume()).isEqualTo(50000000L);
        assertThat(stock.getAverageVolume()).isEqualTo(45000000L);
        assertThat(stock.getLastUpdated()).isEqualTo(lastUpdate);
    }

    @Test
    @DisplayName("Should return null when mapping null Stock to entity")
    void testToEntityWithNull() {
        // Act
        StockEntity entity = stockMapper.toEntity(null);

        // Assert
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should return null when mapping null StockEntity to domain")
    void testToDomainWithNull() {
        // Act
        Stock stock = stockMapper.toDomain(null);

        // Assert
        assertThat(stock).isNull();
    }

    @Test
    @DisplayName("Should correctly map stock with minimal fields")
    void testToEntityWithMinimalFields() {
        // Arrange
        Stock stock = Stock.builder()
                .ticker("GOOGL")
                .currentPrice(new BigDecimal("100.00"))
                .build();

        // Act
        StockEntity entity = stockMapper.toEntity(stock);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getTicker()).isEqualTo("GOOGL");
        assertThat(entity.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(entity.getOpenPrice()).isNull();
        assertThat(entity.getSma20()).isNull();
    }
}
