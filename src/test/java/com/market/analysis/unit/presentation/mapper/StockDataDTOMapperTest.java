package com.market.analysis.unit.presentation.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.presentation.dto.StockDataDTO;
import com.market.analysis.presentation.mapper.StockDataDTOMapper;

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
        LocalDateTime lastUpdated = LocalDateTime.now();
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
}
