package com.market.analysis.unit.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.market.analysis.application.usecase.ManageAnalyzeStockService;
import com.market.analysis.application.usecase.AnalyzeAndPersistStockService;
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
                StockOrigin.EXTERNAL_PROVIDER);
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
    @DisplayName("Should list only analysis-visible stocks")
    void shouldListOnlyAnalysisVisibleStocks() {
        Stock stock = Stock.builder().ticker("AAPL").build();
        StockDataDTO dto = StockDataDTO.builder().ticker("AAPL").build();

        when(stockDataRepository.findAllStocksVisibleInAnalysis()).thenReturn(List.of(stock));
        when(stockDataDTOMapper.toDTO(stock)).thenReturn(dto);

        List<StockDataDTO> result = service.findAllStocks();

        assertThat(result).hasSize(1);
        verify(stockDataRepository, times(1)).findAllStocksVisibleInAnalysis();
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
                .isEqualTo("No se pudo generar una valoración interpretativa válida en este momento. Reintenta más tarde.");
    }

    @Test
    @DisplayName("Should throw StockDataNotFoundException when stock does not exist")
    void shouldThrowWhenStockNotFound() {
        when(stockDataRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(StockDataNotFoundException.class, () -> service.findStockDataById(999L));
    }
}
