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
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.usecase.AnalyzeAndPersistStockService;
import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.HistoricalData;
import com.market.analysis.domain.model.ProhibitedKeyword;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StockOrigin;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.model.TechnicalIndicators;
import com.market.analysis.domain.port.out.ApiCallRateRepository;
import com.market.analysis.domain.port.out.CandleHistoryRepository;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.domain.port.out.HistoricalProviderPort;
import com.market.analysis.domain.port.out.ProhibitedKeywordRepository;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
import com.market.analysis.domain.service.EvaluateStrategyService;
import com.market.analysis.domain.service.ProhibitedKeywordMatcher;
import com.market.analysis.domain.service.StockHistoricalService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyzeAndPersistStockService Tests")
class AnalyzeAndPersistStockServiceTest {

    @Mock
    private StockDataRepository stockDataRepository;
    @Mock
    private StrategyEvaluationRepository strategyEvaluationRepository;
    @Mock
    private ApiCallRateRepository apiCallRateRepository;
    @Mock
    private CandleHistoryRepository candleHistoryRepository;
    @Mock
    private CompanyProfileRepository companyProfileRepository;
    @Mock
    private ProhibitedKeywordRepository prohibitedKeywordRepository;
    @Mock
    private ProhibitedTickerRepository prohibitedTickerRepository;
    @Mock
    private StockProviderPort stockProviderPort;
    @Mock
    private HistoricalProviderPort historicalProviderPort;
    @Mock
    private StockHistoricalService stockHistoricalService;
    @Mock
    private EvaluateStrategyService evaluateStrategyService;
    @Mock
    private ProhibitedKeywordMatcher prohibitedKeywordMatcher;

    @InjectMocks
    private AnalyzeAndPersistStockService pipeline;

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

    @Test
    @DisplayName("Should return ticker as valid when company profile is clean")
    void shouldReturnTickerAsValidWhenCompanyProfileIsClean() {
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .lastUpdated(Instant.now())
                .build();

        when(prohibitedKeywordRepository.findAll()).thenReturn(List.of(ProhibitedKeyword.builder().keyword("ETF").build()));
        when(prohibitedTickerRepository.existsByTicker("AAPL")).thenReturn(false);
        when(companyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(profile));
        when(prohibitedKeywordMatcher.findProhibitionReason(eq("Apple Inc."), any())).thenReturn(null);

        List<String> validTickers = pipeline.validateAndUpdateCompanyProfiles(List.of("AAPL"));

        org.assertj.core.api.Assertions.assertThat(validTickers).containsExactly("AAPL");
        verify(companyProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should persist prohibited ticker when profile matches forbidden keyword")
    void shouldPersistProhibitedTickerWhenProfileMatchesForbiddenKeyword() {
        CompanyProfile profile = CompanyProfile.builder()
                .ticker("SPY")
                .name("SPDR S&P 500 ETF Trust")
                .lastUpdated(Instant.now())
                .build();

        when(prohibitedKeywordRepository.findAll()).thenReturn(List.of(ProhibitedKeyword.builder().keyword("ETF").build()));
        when(prohibitedTickerRepository.existsByTicker("SPY")).thenReturn(false);
        when(companyProfileRepository.findByTicker("SPY")).thenReturn(Optional.of(profile));
        when(prohibitedKeywordMatcher.findProhibitionReason(eq("SPDR S&P 500 ETF Trust"), any())).thenReturn("ETF");

        List<String> validTickers = pipeline.validateAndUpdateCompanyProfiles(List.of("SPY"));

        org.assertj.core.api.Assertions.assertThat(validTickers).isEmpty();
        verify(prohibitedTickerRepository, times(1)).save(any(ProhibitedTicker.class));
    }
}
