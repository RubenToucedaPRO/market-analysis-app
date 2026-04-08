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

    @Test
    @DisplayName("Should correctly map stock with AI valoration to entity")
    void testToEntityWithAIValoration() {
        // Arrange
        String aiValoration = "Esta acción muestra indicadores técnicos fuertes con momentum alcista.";
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(new BigDecimal("150.00"))
                .sma20(new BigDecimal("148.00"))
                .valorationIA(aiValoration)
                .build();

        // Act
        StockEntity entity = stockMapper.toEntity(stock);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getTicker()).isEqualTo("AAPL");
        assertThat(entity.getValorationIA()).isEqualTo(aiValoration);
        assertThat(entity.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should correctly map entity with AI valoration to domain")
    void testToDomainWithAIValoration() {
        // Arrange
        String aiValoration = "Esta acción presenta un análisis técnico favorable con tendencia positiva.";
        Instant lastUpdate = Instant.now();

        StockEntity entity = new StockEntity();
        entity.setId(1L);
        entity.setTicker("MSFT");
        entity.setCurrentPrice(new BigDecimal("200.00"));
        entity.setSma20(new BigDecimal("198.00"));
        entity.setLastUpdate(lastUpdate);
        entity.setValorationIA(aiValoration);

        // Act
        Stock stock = stockMapper.toDomain(entity);

        // Assert
        assertThat(stock).isNotNull();
        assertThat(stock.getTicker()).isEqualTo("MSFT");
        assertThat(stock.getValorationIA()).isEqualTo(aiValoration);
        assertThat(stock.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Should correctly map entity without AI valoration to domain")
    void testToDomainWithoutAIValoration() {
        // Arrange
        Instant lastUpdate = Instant.now();

        StockEntity entity = new StockEntity();
        entity.setId(1L);
        entity.setTicker("TSLA");
        entity.setCurrentPrice(new BigDecimal("250.00"));
        entity.setLastUpdate(lastUpdate);
        entity.setValorationIA(null);

        // Act
        Stock stock = stockMapper.toDomain(entity);

        // Assert
        assertThat(stock).isNotNull();
        assertThat(stock.getTicker()).isEqualTo("TSLA");
        assertThat(stock.getValorationIA()).isNull();
        assertThat(stock.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    @DisplayName("Should map new indicator fields (EMA, RSI, MACD, BB, ATR) from domain to entity")
    void testToEntityWithNewIndicatorFields() {
        // Arrange
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(new BigDecimal("150.00"))
                .ema9(new BigDecimal("149.50"))
                .ema12(new BigDecimal("149.00"))
                .ema20(new BigDecimal("148.00"))
                .ema26(new BigDecimal("147.00"))
                .ema50(new BigDecimal("145.00"))
                .ema200(new BigDecimal("140.00"))
                .rsi14(new BigDecimal("62.50"))
                .rsi30(new BigDecimal("55.00"))
                .macdLine(new BigDecimal("2.00"))
                .macdSignal(new BigDecimal("1.50"))
                .macdHistogram(new BigDecimal("0.50"))
                .bbUpper20(new BigDecimal("155.00"))
                .bbLower20(new BigDecimal("145.00"))
                .atr14(new BigDecimal("3.20"))
                .build();

        // Act
        StockEntity entity = stockMapper.toEntity(stock);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getEma9()).isEqualByComparingTo(new BigDecimal("149.50"));
        assertThat(entity.getEma12()).isEqualByComparingTo(new BigDecimal("149.00"));
        assertThat(entity.getEma20()).isEqualByComparingTo(new BigDecimal("148.00"));
        assertThat(entity.getEma26()).isEqualByComparingTo(new BigDecimal("147.00"));
        assertThat(entity.getEma50()).isEqualByComparingTo(new BigDecimal("145.00"));
        assertThat(entity.getEma200()).isEqualByComparingTo(new BigDecimal("140.00"));
        assertThat(entity.getRsi14()).isEqualByComparingTo(new BigDecimal("62.50"));
        assertThat(entity.getRsi30()).isEqualByComparingTo(new BigDecimal("55.00"));
        assertThat(entity.getMacdLine()).isEqualByComparingTo(new BigDecimal("2.00"));
        assertThat(entity.getMacdSignal()).isEqualByComparingTo(new BigDecimal("1.50"));
        assertThat(entity.getMacdHistogram()).isEqualByComparingTo(new BigDecimal("0.50"));
        assertThat(entity.getBbUpper20()).isEqualByComparingTo(new BigDecimal("155.00"));
        assertThat(entity.getBbLower20()).isEqualByComparingTo(new BigDecimal("145.00"));
        assertThat(entity.getAtr14()).isEqualByComparingTo(new BigDecimal("3.20"));
    }

    @Test
    @DisplayName("Should map new indicator fields (EMA, RSI, MACD, BB, ATR) from entity to domain")
    void testToDomainWithNewIndicatorFields() {
        // Arrange
        StockEntity entity = new StockEntity();
        entity.setId(1L);
        entity.setTicker("MSFT");
        entity.setCurrentPrice(new BigDecimal("300.00"));
        entity.setEma9(new BigDecimal("299.00"));
        entity.setEma12(new BigDecimal("298.00"));
        entity.setEma20(new BigDecimal("295.00"));
        entity.setEma26(new BigDecimal("292.00"));
        entity.setEma50(new BigDecimal("285.00"));
        entity.setEma200(new BigDecimal("260.00"));
        entity.setRsi14(new BigDecimal("70.00"));
        entity.setRsi30(new BigDecimal("65.00"));
        entity.setMacdLine(new BigDecimal("3.50"));
        entity.setMacdSignal(new BigDecimal("2.80"));
        entity.setMacdHistogram(new BigDecimal("0.70"));
        entity.setBbUpper20(new BigDecimal("310.00"));
        entity.setBbLower20(new BigDecimal("290.00"));
        entity.setAtr14(new BigDecimal("4.50"));

        // Act
        Stock stock = stockMapper.toDomain(entity);

        // Assert
        assertThat(stock).isNotNull();
        assertThat(stock.getEma9()).isEqualByComparingTo(new BigDecimal("299.00"));
        assertThat(stock.getEma12()).isEqualByComparingTo(new BigDecimal("298.00"));
        assertThat(stock.getEma20()).isEqualByComparingTo(new BigDecimal("295.00"));
        assertThat(stock.getEma26()).isEqualByComparingTo(new BigDecimal("292.00"));
        assertThat(stock.getEma50()).isEqualByComparingTo(new BigDecimal("285.00"));
        assertThat(stock.getEma200()).isEqualByComparingTo(new BigDecimal("260.00"));
        assertThat(stock.getRsi14()).isEqualByComparingTo(new BigDecimal("70.00"));
        assertThat(stock.getRsi30()).isEqualByComparingTo(new BigDecimal("65.00"));
        assertThat(stock.getMacdLine()).isEqualByComparingTo(new BigDecimal("3.50"));
        assertThat(stock.getMacdSignal()).isEqualByComparingTo(new BigDecimal("2.80"));
        assertThat(stock.getMacdHistogram()).isEqualByComparingTo(new BigDecimal("0.70"));
        assertThat(stock.getBbUpper20()).isEqualByComparingTo(new BigDecimal("310.00"));
        assertThat(stock.getBbLower20()).isEqualByComparingTo(new BigDecimal("290.00"));
        assertThat(stock.getAtr14()).isEqualByComparingTo(new BigDecimal("4.50"));
    }

    @Test
    @DisplayName("Should return null for new indicator fields when not set in entity")
    void testToDomainWithNullNewIndicatorFields() {
        // Arrange
        StockEntity entity = new StockEntity();
        entity.setId(1L);
        entity.setTicker("GOOGL");
        entity.setCurrentPrice(new BigDecimal("100.00"));

        // Act
        Stock stock = stockMapper.toDomain(entity);

        // Assert
        assertThat(stock).isNotNull();
        assertThat(stock.getEma9()).isNull();
        assertThat(stock.getEma12()).isNull();
        assertThat(stock.getEma20()).isNull();
        assertThat(stock.getEma26()).isNull();
        assertThat(stock.getEma50()).isNull();
        assertThat(stock.getEma200()).isNull();
        assertThat(stock.getRsi14()).isNull();
        assertThat(stock.getRsi30()).isNull();
        assertThat(stock.getMacdLine()).isNull();
        assertThat(stock.getMacdSignal()).isNull();
        assertThat(stock.getMacdHistogram()).isNull();
        assertThat(stock.getBbUpper20()).isNull();
        assertThat(stock.getBbLower20()).isNull();
        assertThat(stock.getAtr14()).isNull();
    }
}
