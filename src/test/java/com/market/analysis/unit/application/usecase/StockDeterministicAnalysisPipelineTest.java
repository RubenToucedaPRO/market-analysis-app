package com.market.analysis.unit.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.usecase.StockDeterministicAnalysisPipeline;
import com.market.analysis.domain.model.HistoricalData;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StockOrigin;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.model.TechnicalIndicators;
import com.market.analysis.domain.port.out.ApiCallRateRepository;
import com.market.analysis.domain.port.out.CandleHistoryRepository;
import com.market.analysis.domain.port.out.HistoricalProviderPort;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
import com.market.analysis.domain.service.EvaluateStrategyService;
import com.market.analysis.domain.service.StockHistoricalService;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockDeterministicAnalysisPipeline Tests")
class StockDeterministicAnalysisPipelineTest {

    @Mock
    private StockDataRepository stockDataRepository;
    @Mock
    private StrategyEvaluationRepository strategyEvaluationRepository;
    @Mock
    private ApiCallRateRepository apiCallRateRepository;
    @Mock
    private CandleHistoryRepository candleHistoryRepository;
    @Mock
    private StockProviderPort stockProviderPort;
    @Mock
    private HistoricalProviderPort historicalProviderPort;
    @Mock
    private StockHistoricalService stockHistoricalService;
    @Mock
    private EvaluateStrategyService evaluateStrategyService;

    @InjectMocks
    private StockDeterministicAnalysisPipeline pipeline;

    @Test
    @DisplayName("Should persist and evaluate stock with requested origin")
    void shouldPersistAndEvaluateStock() {
        Strategy strategy = Strategy.builder().id(1L).name("Momentum").build();
        Stock quote = Stock.builder().ticker("AAPL").currentPrice(BigDecimal.valueOf(180)).build();
        Stock saved = Stock.builder().id(10L).ticker("AAPL").build();
        HistoricalData historicalData = HistoricalData.builder().ticker("AAPL").lastUpdate(Instant.now()).build();
        TechnicalIndicators indicators = TechnicalIndicators.builder()
                .sma20(BigDecimal.ONE)
                .sma50(BigDecimal.ONE)
                .sma200(BigDecimal.ONE)
                .currentVolume(100L)
                .averageVolume(200L)
                .lastUpdated(Instant.now())
                .build();
        StrategyEvaluation evaluation = StrategyEvaluation.builder().compliant(true).build();

        when(stockProviderPort.getQuote("AAPL")).thenReturn(quote);
        when(stockDataRepository.findByTickerAndLastUpdateBetween(eq("AAPL"), any(), any())).thenReturn(null);
        when(historicalProviderPort.fetchHistoricalData("AAPL")).thenReturn(historicalData);
        when(stockHistoricalService.calculateIndicators(historicalData, 20)).thenReturn(indicators);
        when(stockDataRepository.save(any(Stock.class))).thenReturn(saved);
        when(evaluateStrategyService.evaluateStrategy(strategy, saved)).thenReturn(evaluation);

        pipeline.analyzeAndPersist("AAPL", strategy, StockOrigin.STRATEGY_SUGGESTION);

        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        verify(stockDataRepository, times(1)).save(stockCaptor.capture());
        verify(strategyEvaluationRepository, times(1)).save(evaluation, saved);
        verify(apiCallRateRepository, times(1)).save(eq("AAPL"), any());
        verify(candleHistoryRepository, never()).saveCandlesForTicker(anyString(), any());

        Stock persisted = stockCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(persisted.getOrigin()).isEqualTo(StockOrigin.STRATEGY_SUGGESTION);
        org.assertj.core.api.Assertions.assertThat(persisted.getStrategyId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return null and skip persistence when quote is missing")
    void shouldSkipWhenQuoteIsMissing() {
        Strategy strategy = Strategy.builder().id(1L).name("Momentum").build();
        when(stockProviderPort.getQuote("AAPL")).thenReturn(null);

        pipeline.analyzeAndPersist("AAPL", strategy, StockOrigin.EXTERNAL_PROVIDER);

        verify(stockDataRepository, never()).save(any());
        verify(strategyEvaluationRepository, never()).save(any(), any());
        verify(historicalProviderPort, never()).fetchHistoricalData(anyString());
    }

    @Test
    @DisplayName("Should reuse cached metrics when stock was already updated today")
    void shouldReuseCachedMetrics() {
        Strategy strategy = Strategy.builder().id(1L).name("Momentum").build();
        Stock quote = Stock.builder().ticker("AAPL").build();
        Stock cached = Stock.builder().ticker("AAPL").sma20(BigDecimal.valueOf(12)).build();
        Stock saved = Stock.builder().id(10L).ticker("AAPL").build();
        StrategyEvaluation evaluation = StrategyEvaluation.builder().compliant(false).build();

        when(stockProviderPort.getQuote("AAPL")).thenReturn(quote);
        when(stockDataRepository.findByTickerAndLastUpdateBetween(eq("AAPL"), any(), any())).thenReturn(cached);
        when(stockDataRepository.save(any(Stock.class))).thenReturn(saved);
        when(evaluateStrategyService.evaluateStrategy(strategy, saved)).thenReturn(evaluation);

        pipeline.analyzeAndPersist("AAPL", strategy, StockOrigin.EXTERNAL_PROVIDER);

        verify(historicalProviderPort, never()).fetchHistoricalData(anyString());
        verify(stockDataRepository, times(1)).save(any(Stock.class));
    }
}
