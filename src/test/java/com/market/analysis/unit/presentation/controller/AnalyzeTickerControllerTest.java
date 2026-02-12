package com.market.analysis.unit.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.market.analysis.application.dto.StockDataDTO;
import com.market.analysis.application.mapper.StockDataDTOMapper;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.presentation.controller.AnalyzeTickerController;

/**
 * Unit tests for AnalyzeTickerController.
 */
@DisplayName("AnalyzeTickerController Unit Tests")
@ExtendWith(MockitoExtension.class)
class AnalyzeTickerControllerTest {

    @Mock
    private ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase;

    @Mock
    private com.market.analysis.domain.port.in.ManageStrategyUseCase manageStrategyUseCase;

    @Mock
    private StockDataDTOMapper stockMapper;

    @Mock
    private com.market.analysis.application.mapper.StrategyDTOMapper strategyMapper;

    @Mock
    private Model model;

    @InjectMocks
    private AnalyzeTickerController controller;

    private StockDataDTO testStockDataDTO;
    private StockDataDTO testStockDTO;
    private com.market.analysis.domain.model.Strategy testStrategy;
    private com.market.analysis.application.dto.StrategyDTO testStrategyDTO;

    @BeforeEach
    void setUp() {
        LocalDateTime lastUpdated = LocalDateTime.now();

        testStockDataDTO = StockDataDTO.builder()
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

        testStrategy = com.market.analysis.domain.model.Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("A test strategy")
                .rules(Arrays.asList())
                .build();

        testStrategyDTO = com.market.analysis.application.dto.StrategyDTO.builder()
                .id(1L)
                .name("Test Strategy")
                .description("A test strategy")
                .rules(Arrays.asList())
                .build();
    }

    @Test
    @DisplayName("Should get all tickers and display analysis page")
    void testGetAllTickers() {
        // Arrange
        StockDataDTO stock2 = StockDataDTO.builder()
                .ticker("GOOGL")
                .currentPrice(new BigDecimal("100.00"))
                .build();

        StockDataDTO stockDTO2 = StockDataDTO.builder()
                .ticker("GOOGL")
                .currentPrice(new BigDecimal("100.00"))
                .build();

        List<StockDataDTO> stocks = Arrays.asList(testStockDataDTO, stock2);
        List<com.market.analysis.application.dto.StrategyDTO> strategiesDTOList = Arrays.asList(testStrategyDTO);

        when(manageAnalyzeTickerUseCase.findAllStocks()).thenReturn(stocks);
        when(manageStrategyUseCase.getAllStrategies()).thenReturn(strategiesDTOList);

        // Act
        String viewName = controller.getAllTickers(model);

        // Assert
        assertThat(viewName).isEqualTo("analysis/analysis");
        verify(manageAnalyzeTickerUseCase, times(1)).findAllStocks();
        verify(manageStrategyUseCase, times(1)).getAllStrategies();
        verify(stockMapper, times(2)).toDTO(any(Stock.class));
        verify(model, times(1)).addAttribute(eq("tickers"), any(List.class));
        verify(model, times(1)).addAttribute(eq("strategies"), any(List.class));
    }

    @Test
    @DisplayName("Should get all tickers with empty list")
    void testGetAllTickersEmpty() {
        // Arrange
        when(manageAnalyzeTickerUseCase.findAllStocks()).thenReturn(Arrays.asList());
        when(manageStrategyUseCase.getAllStrategies()).thenReturn(Arrays.asList());

        // Act
        String viewName = controller.getAllTickers(model);

        // Assert
        assertThat(viewName).isEqualTo("analysis/analysis");
        verify(manageAnalyzeTickerUseCase, times(1)).findAllStocks();
        verify(manageStrategyUseCase, times(1)).getAllStrategies();
        verify(model, times(1)).addAttribute(eq("tickers"), any(List.class));
        verify(model, times(1)).addAttribute(eq("strategies"), any(List.class));
    }

    @Test
    @DisplayName("Should get ticker data and redirect to analysis")
    void testGetTickerData() {
        // Arrange
        String tickers = "AAPL,GOOGL,MSFT";
        Long strategyId = 1L;

        // Act
        String viewName = controller.getTickerData(tickers, strategyId);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/analysis");
        verify(manageAnalyzeTickerUseCase, times(1)).getStockData(tickers, strategyId);
    }

    @Test
    @DisplayName("Should get single ticker data")
    void testGetSingleTickerData() {
        // Arrange
        String ticker = "AAPL";
        Long strategyId = 1L;

        // Act
        String viewName = controller.getTickerData(ticker, strategyId);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/analysis");
        verify(manageAnalyzeTickerUseCase, times(1)).getStockData(ticker, strategyId);
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
        controller.getTickerData("AAPL", 1L);
        controller.updateTicker("AAPL");
        controller.deleteTicker("AAPL");

        verify(manageAnalyzeTickerUseCase, times(1)).getStockData("AAPL", 1L);
        verify(manageAnalyzeTickerUseCase, times(1)).updateStockData("AAPL");
        verify(manageAnalyzeTickerUseCase, times(1)).deleteStockDataByTicker("AAPL");
    }
}
