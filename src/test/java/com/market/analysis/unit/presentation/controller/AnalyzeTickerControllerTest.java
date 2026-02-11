package com.market.analysis.unit.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.presentation.controller.AnalyzeTickerController;
import com.market.analysis.presentation.dto.StockDataDTO;
import com.market.analysis.presentation.mapper.StockDataDTOMapper;

/**
 * Unit tests for AnalyzeTickerController.
 */
@DisplayName("AnalyzeTickerController Unit Tests")
@ExtendWith(MockitoExtension.class)
class AnalyzeTickerControllerTest {

    @Mock
    private ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase;

    @Mock
    private StockDataDTOMapper mapper;

    @Mock
    private Model model;

    @InjectMocks
    private AnalyzeTickerController controller;

    private Stock testStock;
    private StockDataDTO testStockDTO;

    @BeforeEach
    void setUp() {
        LocalDateTime lastUpdated = LocalDateTime.now();
        
        testStock = Stock.builder()
                .ticker("AAPL")
                .logoUrl("https://example.com/logo.png")
                .currentPrice(new BigDecimal("150.50"))
                .openPrice(new BigDecimal("149.00"))
                .volume(50000000L)
                .lastUpdated(lastUpdated)
                .build();

        testStockDTO = StockDataDTO.builder()
                .ticker("AAPL")
                .logoUrl("https://example.com/logo.png")
                .currentPrice(new BigDecimal("150.50"))
                .openPrice(new BigDecimal("149.00"))
                .volume(50000000L)
                .lastUpdated(lastUpdated)
                .build();
    }

    @Test
    @DisplayName("Should get all tickers and display analysis page")
    void testGetAllTickers() {
        // Arrange
        Stock stock2 = Stock.builder()
                .ticker("GOOGL")
                .currentPrice(new BigDecimal("100.00"))
                .build();
        
        StockDataDTO stockDTO2 = StockDataDTO.builder()
                .ticker("GOOGL")
                .currentPrice(new BigDecimal("100.00"))
                .build();
        
        List<Stock> stocks = Arrays.asList(testStock, stock2);
        when(manageAnalyzeTickerUseCase.findAllStocks()).thenReturn(stocks);
        when(mapper.toDTO(testStock)).thenReturn(testStockDTO);
        when(mapper.toDTO(stock2)).thenReturn(stockDTO2);

        // Act
        String viewName = controller.getAllTickers(model);

        // Assert
        assertThat(viewName).isEqualTo("analysis/analysis");
        verify(manageAnalyzeTickerUseCase, times(1)).findAllStocks();
        verify(mapper, times(2)).toDTO(any(Stock.class));
        verify(model, times(1)).addAttribute(anyString(), any(List.class));
    }

    @Test
    @DisplayName("Should get all tickers with empty list")
    void testGetAllTickersEmpty() {
        // Arrange
        when(manageAnalyzeTickerUseCase.findAllStocks()).thenReturn(Arrays.asList());

        // Act
        String viewName = controller.getAllTickers(model);

        // Assert
        assertThat(viewName).isEqualTo("analysis/analysis");
        verify(manageAnalyzeTickerUseCase, times(1)).findAllStocks();
        verify(model, times(1)).addAttribute(anyString(), any(List.class));
    }

    @Test
    @DisplayName("Should get ticker data and redirect to analysis")
    void testGetTickerData() {
        // Arrange
        String tickers = "AAPL,GOOGL,MSFT";

        // Act
        String viewName = controller.getTickerData(tickers);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/analysis");
        verify(manageAnalyzeTickerUseCase, times(1)).getStockData(tickers);
    }

    @Test
    @DisplayName("Should get single ticker data")
    void testGetSingleTickerData() {
        // Arrange
        String ticker = "AAPL";

        // Act
        String viewName = controller.getTickerData(ticker);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/analysis");
        verify(manageAnalyzeTickerUseCase, times(1)).getStockData(ticker);
    }

    @Test
    @DisplayName("Should update ticker and redirect to analysis")
    void testUpdateTicker() {
        // Arrange
        String ticker = "AAPL";

        // Act
        String viewName = controller.updateTicker(ticker);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/analysis");
        verify(manageAnalyzeTickerUseCase, times(1)).updateStockData(ticker);
    }

    @Test
    @DisplayName("Should delete ticker and redirect to analysis")
    void testDeleteTicker() {
        // Arrange
        String ticker = "AAPL";

        // Act
        String viewName = controller.deleteTicker(ticker);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/analysis");
        verify(manageAnalyzeTickerUseCase, times(1)).deleteStockDataByTicker(ticker);
    }

    @Test
    @DisplayName("Should handle multiple operations correctly")
    void testMultipleOperations() {
        // Test create, update, and delete in sequence
        controller.getTickerData("AAPL");
        controller.updateTicker("AAPL");
        controller.deleteTicker("AAPL");

        verify(manageAnalyzeTickerUseCase, times(1)).getStockData("AAPL");
        verify(manageAnalyzeTickerUseCase, times(1)).updateStockData("AAPL");
        verify(manageAnalyzeTickerUseCase, times(1)).deleteStockDataByTicker("AAPL");
    }
}
