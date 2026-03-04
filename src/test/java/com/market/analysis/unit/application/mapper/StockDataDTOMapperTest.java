package com.market.analysis.unit.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.application.dto.StockDataDTO;
import com.market.analysis.application.mapper.StockDataDTOMapper;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyEvaluation;

/**
 * Unit tests for StockDataDTOMapper.
 */
@DisplayName("StockDataDTOMapper Unit Tests")
class StockDataDTOMapperTest {

    private StockDataDTOMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new StockDataDTOMapper();
    }

    @Test
    @DisplayName("Should map Stock domain to StockDataDTO")
    void testToDTO() {
        // Arrange
        Instant lastUpdated = Instant.now();
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
                .lastUpdated(lastUpdated)
                .build();

        // Act
        StockDataDTO dto = mapper.toDTO(stock);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getTicker()).isEqualTo("AAPL");
        assertThat(dto.getLogoUrl()).isEqualTo("https://example.com/logo.png");
        assertThat(dto.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("150.50"));
        assertThat(dto.getOpenPrice()).isEqualByComparingTo(new BigDecimal("149.00"));
        assertThat(dto.getHighOfDay()).isEqualByComparingTo(new BigDecimal("151.00"));
        assertThat(dto.getLowOfDay()).isEqualByComparingTo(new BigDecimal("148.50"));
        assertThat(dto.getPreviousClose()).isEqualByComparingTo(new BigDecimal("148.00"));
        assertThat(dto.getSma20()).isEqualByComparingTo(new BigDecimal("147.00"));
        assertThat(dto.getSma50()).isEqualByComparingTo(new BigDecimal("145.00"));
        assertThat(dto.getSma200()).isEqualByComparingTo(new BigDecimal("140.00"));
        assertThat(dto.getVolume()).isEqualTo(50000000L);
        assertThat(dto.getAverageVolume()).isEqualTo(45000000L);
        assertThat(dto.getLastUpdated()).isEqualTo(lastUpdated);
    }

    @Test
    @DisplayName("Should return null when mapping null Stock")
    void testToDTOWithNull() {
        // Act
        StockDataDTO dto = mapper.toDTO(null);

        // Assert
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("Should correctly map stock with minimal fields")
    void testToDTOWithMinimalFields() {
        // Arrange
        Stock stock = Stock.builder()
                .ticker("GOOGL")
                .currentPrice(new BigDecimal("100.00"))
                .build();

        // Act
        StockDataDTO dto = mapper.toDTO(stock);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getTicker()).isEqualTo("GOOGL");
        assertThat(dto.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(dto.getLogoUrl()).isNull();
        assertThat(dto.getOpenPrice()).isNull();
        assertThat(dto.getSma20()).isNull();
    }

    @Test
    @DisplayName("Should correctly map stock without logo")
    void testToDTOWithoutLogo() {
        // Arrange
        Stock stock = Stock.builder()
                .ticker("MSFT")
                .currentPrice(new BigDecimal("200.00"))
                .volume(10000000L)
                .build();

        // Act
        StockDataDTO dto = mapper.toDTO(stock);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getTicker()).isEqualTo("MSFT");
        assertThat(dto.getLogoUrl()).isNull();
        assertThat(dto.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(dto.getVolume()).isEqualTo(10000000L);
    }

    @Test
    @DisplayName("Should correctly map stock with AI valoration")
    void testToDTOWithAIValoration() {
        // Arrange
        String aiValoration = "Esta acción muestra indicadores técnicos fuertes con momentum alcista y buen volumen.";
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(new BigDecimal("150.00"))
                .sma20(new BigDecimal("148.00"))
                .sma50(new BigDecimal("145.00"))
                .sma200(new BigDecimal("140.00"))
                .volume(50000000L)
                .averageVolume(45000000L)
                .valorationIA(aiValoration)
                .build();

        // Act
        StockDataDTO dto = mapper.toDTO(stock);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getTicker()).isEqualTo("AAPL");
        assertThat(dto.getValorationIA()).isEqualTo(aiValoration);
        assertThat(dto.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should correctly map stock without AI valoration")
    void testToDTOWithoutAIValoration() {
        // Arrange
        Stock stock = Stock.builder()
                .ticker("TSLA")
                .currentPrice(new BigDecimal("250.00"))
                .volume(20000000L)
                .build();

        // Act
        StockDataDTO dto = mapper.toDTO(stock);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getTicker()).isEqualTo("TSLA");
        assertThat(dto.getValorationIA()).isNull();
        assertThat(dto.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    @DisplayName("Should correctly map risk-reward fields from strategy evaluation")
    void testToDTOWithRiskRewardFields() {
        // Arrange
        StrategyEvaluation evaluation = StrategyEvaluation.builder()
                .strategyName("Test Strategy")
                .compliant(true)
                .complianceRate(new BigDecimal("100.00"))
                .targetPrice(new BigDecimal("165.00"))
                .stopLossPrice(new BigDecimal("145.00"))
                .riskRewardRatio(new BigDecimal("1.5000"))
                .recommendedShares(100)
                .build();

        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(new BigDecimal("150.00"))
                .strategyEvaluation(evaluation)
                .build();

        // Act
        StockDataDTO dto = mapper.toDTO(stock);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getTargetPrice()).isEqualByComparingTo(new BigDecimal("165.00"));
        assertThat(dto.getStopLossPrice()).isEqualByComparingTo(new BigDecimal("145.00"));
        assertThat(dto.getRiskRewardRatio()).isEqualByComparingTo(new BigDecimal("1.5000"));
        assertThat(dto.getRecommendedShares()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should map null risk-reward fields when no strategy evaluation")
    void testToDTOWithNullRiskRewardWhenNoEvaluation() {
        // Arrange
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(new BigDecimal("150.00"))
                .build();

        // Act
        StockDataDTO dto = mapper.toDTO(stock);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getTargetPrice()).isNull();
        assertThat(dto.getStopLossPrice()).isNull();
        assertThat(dto.getRiskRewardRatio()).isNull();
        assertThat(dto.getRecommendedShares()).isNull();
    }
}
