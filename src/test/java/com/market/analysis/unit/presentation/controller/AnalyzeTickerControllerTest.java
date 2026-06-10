package com.market.analysis.unit.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;

import com.market.analysis.application.dto.CandleChartDTO;
import com.market.analysis.application.dto.CandleDTO;
import com.market.analysis.application.dto.StockDataDTO;
import com.market.analysis.application.mapper.StockDataDTOMapper;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.presentation.controller.AnalyzeTickerController;
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.util.WebConstants;

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
    private MessageSource messageSource;

    @Mock
    private Model model;

    @Mock
    private org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes;

    @InjectMocks
    private AnalyzeTickerController controller;

    private StockDataDTO testStockDataDTO;
    private com.market.analysis.application.dto.StrategyDTO testStrategyDTO;

    @BeforeEach
    void setUp() {
        Instant lastUpdated = Instant.now();

        testStockDataDTO = StockDataDTO.builder()
                .ticker("AAPL")
                .logoUrl("https://example.com/logo.png")
                .currentPrice(new BigDecimal("150.50"))
                .openPrice(new BigDecimal("149.00"))
                .volume(50000000L)
                .lastUpdated(lastUpdated)
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
        when(messageSource.getMessage("ticker.added", null, Locale.getDefault()))
                .thenReturn("Ticker(s) añadidos y analizados correctamente.");

        // Act
        String viewName = controller.getTickerData(tickers, strategyId, redirectAttributes);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/analysis");
        verify(manageAnalyzeTickerUseCase, times(1)).getStockData(tickers, strategyId);
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Ticker(s) añadidos y analizados correctamente."));
    }

    @Test
    @DisplayName("Should get single ticker data")
    void testGetSingleTickerData() {
        // Arrange
        String ticker = "AAPL";
        Long strategyId = 1L;

        // Act
        String viewName = controller.getTickerData(ticker, strategyId, redirectAttributes);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/analysis");
        verify(manageAnalyzeTickerUseCase, times(1)).getStockData(ticker, strategyId);
    }

    @Test
    @DisplayName("Should update ticker and redirect to analysis")
    void testUpdateTicker() {
        // Arrange
        Long id = 1L;
        when(messageSource.getMessage("ticker.updated", null, Locale.getDefault()))
                .thenReturn("Datos del ticker actualizados correctamente.");

        // Act
        String viewName = controller.updateTicker(id, redirectAttributes);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/analysis");
        verify(manageAnalyzeTickerUseCase, times(1)).updateStockData(id);
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Datos del ticker actualizados correctamente."));
    }

    @Test
    @DisplayName("Should update ticker from detail and redirect to ticker detail page")
    void testUpdateTickerFromDetail() {
        // Arrange
        Long id = 1L;
        when(messageSource.getMessage("ticker.updated", null, Locale.getDefault()))
                .thenReturn("Datos del ticker actualizados correctamente.");

        // Act
        String viewName = controller.updateTickerFromDetail(id, redirectAttributes);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/analysis/ticker/1");
        verify(manageAnalyzeTickerUseCase, times(1)).updateStockData(id);
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Datos del ticker actualizados correctamente."));
    }

    @Test
    @DisplayName("Should delete ticker and redirect to analysis")
    void testDeleteTicker() {
        // Arrange
        Long id = 1L;
        when(messageSource.getMessage("ticker.deleted", new Object[] { "AAPL" }, Locale.getDefault()))
                .thenReturn("Ticker 'AAPL' eliminado correctamente.");

        // Act
        String viewName = controller.deleteTicker(id, "AAPL", redirectAttributes);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/analysis");
        verify(manageAnalyzeTickerUseCase, times(1)).deleteById(id, "AAPL");
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Ticker 'AAPL' eliminado correctamente."));
    }

    @Test
    @DisplayName("Should handle multiple operations correctly")
    void testMultipleOperations() {
        // Test create, update, and delete in sequence
        controller.getTickerData("AAPL", 1L, redirectAttributes);
        controller.updateTicker(1L, redirectAttributes);
        controller.deleteTicker(1L, "AAPL", redirectAttributes);

        verify(manageAnalyzeTickerUseCase, times(1)).getStockData("AAPL", 1L);
        verify(manageAnalyzeTickerUseCase, times(1)).updateStockData(1L);
        verify(manageAnalyzeTickerUseCase, times(1)).deleteById(1L, "AAPL");
    }

    @Test
    @DisplayName("Should get ticker detail and display detail page")
    void testGetTickerDetail() {
        // Arrange
        Long id = 1L;
        when(manageAnalyzeTickerUseCase.findStockDataById(id)).thenReturn(testStockDataDTO);

        // Act
        String viewName = controller.getTickerDetail(id, model);

        // Assert
        assertThat(viewName).isEqualTo("analysis/ticker-detail");
        verify(manageAnalyzeTickerUseCase, times(1)).findStockDataById(id);
        verify(model, times(1)).addAttribute("ticker", testStockDataDTO);
    }

    @Test
    @DisplayName("Should get AI valoration and redirect to analysis page")
    void testGetValorationIA() {
        // Arrange
        Long id = 1L;
        when(manageAnalyzeTickerUseCase.getValorationIA(id)).thenReturn(true);
        when(messageSource.getMessage("ticker.ia.success", null, Locale.getDefault()))
                .thenReturn("Valoración IA generada y guardada correctamente.");

        // Act
        String viewName = controller.getValorationIA(id, redirectAttributes);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/analysis/ticker/1");
        verify(manageAnalyzeTickerUseCase, times(1)).getValorationIA(id);
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Valoración IA generada y guardada correctamente."));
    }

    @Test
    @DisplayName("Should handle AI valoration request for different stock IDs")
    void testGetValorationIAWithDifferentIds() {
        // Arrange
        Long id1 = 5L;
        Long id2 = 10L;
        when(manageAnalyzeTickerUseCase.getValorationIA(id1)).thenReturn(true);
        when(manageAnalyzeTickerUseCase.getValorationIA(id2)).thenReturn(true);
        when(messageSource.getMessage("ticker.ia.success", null, Locale.getDefault()))
                .thenReturn("Valoración IA generada y guardada correctamente.");

        // Act
        String viewName1 = controller.getValorationIA(id1, redirectAttributes);
        String viewName2 = controller.getValorationIA(id2, redirectAttributes);

        // Assert
        assertThat(viewName1).isEqualTo("redirect:/analysis/ticker/5");
        assertThat(viewName2).isEqualTo("redirect:/analysis/ticker/10");
        verify(manageAnalyzeTickerUseCase, times(1)).getValorationIA(id1);
        verify(manageAnalyzeTickerUseCase, times(1)).getValorationIA(id2);
    }

    @Test
    @DisplayName("Should show error notification when AI valoration falls back")
    void testGetValorationIAFallbackShowsErrorNotification() {
        Long id = 7L;
        when(manageAnalyzeTickerUseCase.getValorationIA(id)).thenReturn(false);
        when(messageSource.getMessage("ticker.ia.failed", null, Locale.getDefault()))
                .thenReturn("No se pudo generar una valoración IA válida. Se guardó un mensaje de fallback.");

        String viewName = controller.getValorationIA(id, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/analysis/ticker/7");
        verify(manageAnalyzeTickerUseCase, times(1)).getValorationIA(id);
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.error("No se pudo generar una valoración IA válida. Se guardó un mensaje de fallback."));
    }

    // -------------------------------------------------------------------------
    // F2.7 — GET /analysis/ticker/{id}/candles (JSON endpoint)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("F2.7: getCandleChart should delegate to use case and return CandleChartDTO")
    void getCandleChart_delegatesToUseCase_returnsCandleChartDTO() {
        Long id = 1L;
        CandleChartDTO expected = CandleChartDTO.builder()
                .ticker("AAPL")
                .candles(List.of(
                        CandleDTO.builder()
                                .time(1705276800L)
                                .open(new BigDecimal("181.00"))
                                .high(new BigDecimal("183.50"))
                                .low(new BigDecimal("180.00"))
                                .close(new BigDecimal("182.75"))
                                .volume(55_000_000L)
                                .build()))
                .sma20(new BigDecimal("150.00"))
                .sma50(new BigDecimal("145.00"))
                .sma200(new BigDecimal("140.00"))
                .build();
        when(manageAnalyzeTickerUseCase.findCandlesByStockId(id)).thenReturn(expected);

        CandleChartDTO result = controller.getCandleChart(id);

        assertThat(result).isEqualTo(expected);
        assertThat(result.getTicker()).isEqualTo("AAPL");
        assertThat(result.getCandles()).hasSize(1);
        verify(manageAnalyzeTickerUseCase, times(1)).findCandlesByStockId(id);
    }

    @Test
    @DisplayName("F2.7: getCandleChart should return empty candle list when no candles")
    void getCandleChart_noCandles_returnsEmptyList() {
        Long id = 2L;
        CandleChartDTO empty = CandleChartDTO.builder().ticker("TSLA").candles(List.of()).build();
        when(manageAnalyzeTickerUseCase.findCandlesByStockId(id)).thenReturn(empty);

        CandleChartDTO result = controller.getCandleChart(id);

        assertThat(result.getCandles()).isEmpty();
        verify(manageAnalyzeTickerUseCase, times(1)).findCandlesByStockId(id);
    }

    // -------------------------------------------------------------------------
    // F2.8 — GET /analysis/ticker/{id}/chart (Thymeleaf view)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("F2.8: getTickerChart should add ticker to model and return ticker-chart view")
    void getTickerChart_addsTickerToModel_returnsChartView() {
        Long id = 1L;
        when(manageAnalyzeTickerUseCase.findStockDataById(id)).thenReturn(testStockDataDTO);

        String viewName = controller.getTickerChart(id, model);

        assertThat(viewName).isEqualTo("analysis/ticker-chart");
        verify(manageAnalyzeTickerUseCase, times(1)).findStockDataById(id);
        verify(model, times(1)).addAttribute("ticker", testStockDataDTO);
    }

    @Test
    @DisplayName("F2.8: getTickerChart calls findStockDataById with correct id")
    void getTickerChart_callsFindStockDataById_withCorrectId() {
        Long id = 99L;
        StockDataDTO otherStock = StockDataDTO.builder().ticker("MSFT").build();
        when(manageAnalyzeTickerUseCase.findStockDataById(id)).thenReturn(otherStock);

        String viewName = controller.getTickerChart(id, model);

        assertThat(viewName).isEqualTo("analysis/ticker-chart");
        verify(manageAnalyzeTickerUseCase, times(1)).findStockDataById(99L);
    }
}
