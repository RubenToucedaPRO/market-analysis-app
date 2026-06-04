package com.market.analysis.unit.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.dto.CandleChartDTO;
import com.market.analysis.application.dto.StockDataDTO;
import com.market.analysis.application.mapper.CandleDTOMapper;
import com.market.analysis.application.mapper.StockDataDTOMapper;
import com.market.analysis.application.usecase.AnalyzeAndPersistStockService;
import com.market.analysis.application.usecase.ManageAnalyzeStockService;
import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StockOrigin;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.port.out.ApiIAPort;
import com.market.analysis.domain.port.out.CandleHistoryRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.service.PromptBuilder;
import com.market.analysis.domain.service.PromptResponseValidator;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManageAnalyzeStockService Tests")
class ManageAnalyzeStockServiceTest {

    private static final int MAX_PROMPT_CHARS = 4000;
    private static final int OVERSIZED_PROMPT_CHARS = 4500;
    private static final String IA_FALLBACK_VALORATION =
            "No se pudo generar una valoración interpretativa válida en este momento. Reintenta más tarde.";

    @Mock
    private StockDataRepository stockDataRepository;
    @Mock
    private CandleHistoryRepository candleHistoryRepository;
    @Mock
    private StrategyRepository strategyRepository;
    @Mock
    private StockProviderPort stockProviderPort;
    @Mock
    private ApiIAPort apiIAPort;
    @Mock
    private StockDataDTOMapper stockDataDTOMapper;
    @Mock
    private CandleDTOMapper candleDTOMapper;
    @Mock
    private AnalyzeAndPersistStockService analyzeAndPersistStockService;
    @Mock
    private PromptBuilder promptBuilder;
    @Mock
    private PromptResponseValidator promptResponseValidator;

    @InjectMocks
    private ManageAnalyzeStockService service;

    @Test
    @DisplayName("Should throw IllegalArgumentException when strategy id is null")
    void shouldThrowWhenStrategyIdIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getStockData("AAPL", null));

        assertThat(ex.getMessage()).isEqualTo("Strategy ID is required");
        verify(strategyRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should delegate deterministic evaluation to shared pipeline for valid tickers")
    void shouldDelegateToPipelineForValidTickers() {
        Long strategyId = 1L;
        Strategy strategy = Strategy.builder().id(strategyId).name("Momentum").build();

        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(strategy));
        when(analyzeAndPersistStockService.validateAndUpdateCompanyProfiles(List.of("AAPL")))
            .thenReturn(List.of("AAPL"));

        service.getStockData("AAPL", strategyId);

        verify(analyzeAndPersistStockService, times(1)).analyzeAndPersist(
                "AAPL",
                strategy,
                StockOrigin.ANALYSIS);
    }

    @Test
    @DisplayName("Should skip analysis when pipeline returns no valid tickers")
    void shouldSkipWhenPipelineReturnsNoValidTickers() {
        Long strategyId = 1L;
        Strategy strategy = Strategy.builder().id(strategyId).name("Momentum").build();

        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(strategy));
        when(analyzeAndPersistStockService.validateAndUpdateCompanyProfiles(List.of("SPY")))
                .thenReturn(List.of());

        service.getStockData("SPY", strategyId);

        verify(analyzeAndPersistStockService, never()).analyzeAndPersist(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when strategy does not exist")
    void shouldThrowWhenStrategyNotFound() {
        when(strategyRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getStockData("AAPL", 1L));

        assertThat(ex.getMessage()).isEqualTo("Strategy not found with id: 1");
    }

    @Test
    @DisplayName("Should normalize and analyze multiple tickers")
    void shouldNormalizeAndAnalyzeMultipleTickers() {
        Long strategyId = 1L;
        Strategy strategy = Strategy.builder().id(strategyId).name("Momentum").build();

        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(strategy));
        when(analyzeAndPersistStockService.validateAndUpdateCompanyProfiles(List.of("AAPL", "MSFT")))
                .thenReturn(List.of("AAPL", "MSFT"));

        service.getStockData(" aapl , msft ", strategyId);

        verify(analyzeAndPersistStockService, times(1)).analyzeAndPersist("AAPL", strategy, StockOrigin.ANALYSIS);
        verify(analyzeAndPersistStockService, times(1)).analyzeAndPersist("MSFT", strategy, StockOrigin.ANALYSIS);
    }

    @Test
    @DisplayName("Should list only analysis-visible stocks")
    void shouldListOnlyAnalysisVisibleStocks() {
        Stock stock = Stock.builder().ticker("AAPL").build();
        StockDataDTO dto = StockDataDTO.builder().ticker("AAPL").build();

        when(stockDataRepository.findAllStocksVisibleInAnalysis()).thenReturn(List.of(stock));
        when(stockDataDTOMapper.toDTO(stock)).thenReturn(dto);

        List<StockDataDTO> result = service.findAllStocks();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(dto);
        verify(stockDataRepository, times(1)).findAllStocksVisibleInAnalysis();
    }

    @Test
    @DisplayName("Should return mapped stock data by id")
    void shouldReturnStockDataById() {
        Stock stock = Stock.builder().id(10L).ticker("AAPL").build();
        StockDataDTO dto = StockDataDTO.builder().id(10L).ticker("AAPL").build();

        when(stockDataRepository.findById(10L)).thenReturn(Optional.of(stock));
        when(stockDataDTOMapper.toDTO(stock)).thenReturn(dto);

        StockDataDTO result = service.findStockDataById(10L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Should update stock prices when quote is available")
    void shouldUpdateStockDataWhenQuoteExists() {
        Stock existing = Stock.builder().id(10L).ticker("AAPL").build();
        Stock quote = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(180))
                .openPrice(BigDecimal.valueOf(178))
                .highOfDay(BigDecimal.valueOf(181))
                .lowOfDay(BigDecimal.valueOf(177))
                .previousClose(BigDecimal.valueOf(176))
                .build();

        when(stockDataRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(quote);

        service.updateStockData(10L);

        verify(stockDataRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("Should skip stock save when quote is not available")
    void shouldSkipUpdateWhenQuoteDoesNotExist() {
        Stock existing = Stock.builder().id(10L).ticker("AAPL").build();

        when(stockDataRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(null);

        service.updateStockData(10L);

        verify(stockDataRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete candles when removing the last stock for ticker")
    void shouldDeleteCandlesWhenDeletingLastStockForTicker() {
        when(stockDataRepository.existsByTicker("AAPL")).thenReturn(false);

        service.deleteById(10L, "AAPL");

        verify(stockDataRepository, times(1)).deleteById(10L);
        verify(candleHistoryRepository, times(1)).deleteCandlesByTicker("AAPL");
    }

    @Test
    @DisplayName("Should keep candles when stock data still exists for ticker")
    void shouldKeepCandlesWhenTickerStillExists() {
        when(stockDataRepository.existsByTicker("AAPL")).thenReturn(true);

        service.deleteById(10L, "AAPL");

        verify(candleHistoryRepository, never()).deleteCandlesByTicker(any());
    }

    @Test
    @DisplayName("Should build and return candle chart data")
    void shouldFindCandlesByStockId() {
        Stock stock = Stock.builder().id(10L).ticker("AAPL").build();
        CandleChartDTO chartDTO = CandleChartDTO.builder().build();

        when(stockDataRepository.findById(10L)).thenReturn(Optional.of(stock));
        when(candleHistoryRepository.findCandlesByTicker("AAPL")).thenReturn(List.of());
        when(candleDTOMapper.toChartDTO(stock, List.of())).thenReturn(chartDTO);

        CandleChartDTO result = service.findCandlesByStockId(10L);

        assertThat(result).isEqualTo(chartDTO);
    }

    @Test
    @DisplayName("Should throw StockDataNotFoundException when candles stock does not exist")
    void shouldThrowWhenFindingCandlesForMissingStock() {
        when(stockDataRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(StockDataNotFoundException.class, () -> service.findCandlesByStockId(10L));
    }

    @Test
    @DisplayName("Should save generated valoration when first AI response is valid")
    void shouldSaveValorationWhenFirstResponseIsValid() {
        Long stockId = 10L;
        StrategyEvaluation strategyEvaluation = StrategyEvaluation.builder().strategyName("S").build();
        Stock stock = Stock.builder().id(stockId).ticker("AAPL").strategyEvaluation(strategyEvaluation).build();

        when(stockDataRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(promptBuilder.buildAnalysisPrompt(any(), any())).thenReturn("prompt");
        when(apiIAPort.getValoration("prompt")).thenReturn("valid");
        when(promptResponseValidator.isValid("valid")).thenReturn(true);

        boolean generated = service.getValorationIA(stockId);

        assertThat(generated).isTrue();
        assertThat(stock.getValorationIA()).isEqualTo("valid");
    }

    @Test
    @DisplayName("Should save generated valoration when retry response is valid")
    void shouldSaveValorationWhenRetryResponseIsValid() {
        Long stockId = 10L;
        StrategyEvaluation strategyEvaluation = StrategyEvaluation.builder().strategyName("S").build();
        Stock stock = Stock.builder().id(stockId).ticker("AAPL").strategyEvaluation(strategyEvaluation).build();

        when(stockDataRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(promptBuilder.buildAnalysisPrompt(any(), any())).thenReturn("prompt");
        when(apiIAPort.getValoration("prompt")).thenReturn("invalid");
        when(promptResponseValidator.isValid("invalid")).thenReturn(false);
        when(promptResponseValidator.buildRetryPrompt("prompt")).thenReturn("retry-prompt");
        when(apiIAPort.getValoration("retry-prompt")).thenReturn("valid-retry");
        when(promptResponseValidator.isValid("valid-retry")).thenReturn(true);

        boolean generated = service.getValorationIA(stockId);

        assertThat(generated).isTrue();
        assertThat(stock.getValorationIA()).isEqualTo("valid-retry");
    }

    @Test
    @DisplayName("Should truncate oversized prompt before requesting AI valoration")
    void shouldTruncatePromptBeforeRequestingAiValoration() {
        Long stockId = 10L;
        StrategyEvaluation strategyEvaluation = StrategyEvaluation.builder().strategyName("S").build();
        Stock stock = Stock.builder().id(stockId).ticker("AAPL").strategyEvaluation(strategyEvaluation).build();
        String oversizedPrompt = "x".repeat(OVERSIZED_PROMPT_CHARS);

        when(stockDataRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(promptBuilder.buildAnalysisPrompt(any(), any())).thenReturn(oversizedPrompt);
        when(apiIAPort.getValoration(any())).thenReturn("valid");
        when(promptResponseValidator.isValid("valid")).thenReturn(true);

        boolean generated = service.getValorationIA(stockId);

        assertThat(generated).isTrue();
        verify(apiIAPort, times(1)).getValoration(eq(oversizedPrompt.substring(0, MAX_PROMPT_CHARS)));
    }

    @Test
    @DisplayName("Should save fallback valoration when AI request throws exception")
    void shouldSaveFallbackWhenAiRequestThrowsException() {
        Long stockId = 10L;
        StrategyEvaluation strategyEvaluation = StrategyEvaluation.builder().strategyName("S").build();
        Stock stock = Stock.builder().id(stockId).ticker("AAPL").strategyEvaluation(strategyEvaluation).build();

        when(stockDataRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(promptBuilder.buildAnalysisPrompt(any(), any())).thenReturn("prompt");
        when(apiIAPort.getValoration("prompt")).thenThrow(new RuntimeException("boom"));

        boolean generated = service.getValorationIA(stockId);

        assertThat(generated).isFalse();
        assertThat(stock.getValorationIA())
                .isEqualTo(IA_FALLBACK_VALORATION);
    }

    @Test
    @DisplayName("Should save fallback valoration when responses are invalid")
    void shouldSaveFallbackValorationWhenInvalidResponses() {
        Long stockId = 10L;
        StrategyEvaluation strategyEvaluation = StrategyEvaluation.builder().strategyName("S").build();
        Stock stock = Stock.builder().id(stockId).ticker("AAPL").strategyEvaluation(strategyEvaluation).build();

        when(stockDataRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(promptBuilder.buildAnalysisPrompt(any(), any())).thenReturn("prompt");
        when(apiIAPort.getValoration("prompt")).thenReturn("invalid");
        when(promptResponseValidator.isValid("invalid")).thenReturn(false);
        when(promptResponseValidator.buildRetryPrompt("prompt")).thenReturn("retry-prompt");
        when(apiIAPort.getValoration("retry-prompt")).thenReturn("still-invalid");
        when(promptResponseValidator.isValid("still-invalid")).thenReturn(false);

        boolean generated = service.getValorationIA(stockId);

        assertThat(generated).isFalse();
        verify(stockDataRepository, times(1)).save(stock);
        assertThat(stock.getValorationIA())
                .isEqualTo(IA_FALLBACK_VALORATION);
    }

    @Test
    @DisplayName("Should throw StockDataNotFoundException when stock does not exist")
    void shouldThrowWhenStockNotFound() {
        when(stockDataRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(StockDataNotFoundException.class, () -> service.findStockDataById(999L));
    }
}
