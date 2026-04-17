package com.market.analysis.unit.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.usecase.DefaultDeterministicTickerEvaluator;
import com.market.analysis.application.usecase.DeterministicTickerEvaluation;
import com.market.analysis.domain.model.HistoricalData;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.model.TechnicalIndicators;
import com.market.analysis.domain.port.out.HistoricalProviderPort;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.domain.service.EvaluateStrategyService;
import com.market.analysis.domain.service.StockHistoricalService;

@DisplayName("DefaultDeterministicTickerEvaluator Unit Tests")
@ExtendWith(MockitoExtension.class)
class DefaultDeterministicTickerEvaluatorTest {

    @Mock
    private StockProviderPort stockProviderPort;
    @Mock
    private HistoricalProviderPort historicalProviderPort;
    @Mock
    private StockHistoricalService stockHistoricalService;
    @Mock
    private EvaluateStrategyService evaluateStrategyService;

    @InjectMocks
    private DefaultDeterministicTickerEvaluator evaluator;

    @Test
    @DisplayName("Should evaluate ticker as suitable when deterministic evaluation is compliant")
    void shouldEvaluateTickerAsSuitable() {
        Strategy strategy = Strategy.builder().id(1L).name("S1").rules(List.of()).build();
        Stock stock = Stock.builder().ticker("AAPL").currentPrice(BigDecimal.valueOf(120)).build();
        HistoricalData historicalData = HistoricalData.builder().ticker("AAPL").candles(List.of()).build();
        TechnicalIndicators indicators = TechnicalIndicators.builder()
                .sma20(BigDecimal.TEN)
                .sma50(BigDecimal.TEN)
                .sma200(BigDecimal.TEN)
                .averageVolume(100L)
                .currentVolume(120L)
                .lastUpdated(Instant.now())
                .build();
        StrategyEvaluation strategyEvaluation = StrategyEvaluation.builder()
                .compliant(true)
                .summary("Cumple reglas")
                .build();

        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(historicalProviderPort.fetchHistoricalData("AAPL")).thenReturn(historicalData);
        when(stockHistoricalService.calculateIndicators(historicalData, 20)).thenReturn(indicators);
        when(evaluateStrategyService.evaluateStrategy(strategy, stock)).thenReturn(strategyEvaluation);

        DeterministicTickerEvaluation result = evaluator.evaluate("AAPL", strategy);

        assertThat(result.isSuitable()).isTrue();
        assertThat(result.getTraceability()).containsExactly("Cumple reglas");
        verify(evaluateStrategyService).evaluateStrategy(strategy, stock);
    }

    @Test
    @DisplayName("Should mark as not suitable when quote is unavailable")
    void shouldReturnNotSuitableWhenQuoteMissing() {
        Strategy strategy = Strategy.builder().id(1L).name("S1").rules(List.of()).build();
        when(stockProviderPort.getQuote("AAPL")).thenReturn(null);

        DeterministicTickerEvaluation result = evaluator.evaluate("AAPL", strategy);

        assertThat(result.isSuitable()).isFalse();
        assertThat(result.getTraceability()).containsExactly("No se pudo obtener cotización para el ticker.");
    }

    @Test
    @DisplayName("Should mark as not suitable when historical data is unavailable")
    void shouldReturnNotSuitableWhenHistoricalDataMissing() {
        Strategy strategy = Strategy.builder().id(1L).name("S1").rules(List.of()).build();
        Stock stock = Stock.builder().ticker("AAPL").currentPrice(BigDecimal.valueOf(120)).build();
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(historicalProviderPort.fetchHistoricalData("AAPL")).thenReturn(null);

        DeterministicTickerEvaluation result = evaluator.evaluate("AAPL", strategy);

        assertThat(result.isSuitable()).isFalse();
        assertThat(result.getTraceability()).containsExactly("No se pudieron obtener datos históricos para el ticker.");
    }

    @Test
    @DisplayName("Should mark as not suitable when technical indicators cannot be calculated")
    void shouldReturnNotSuitableWhenIndicatorsMissing() {
        Strategy strategy = Strategy.builder().id(1L).name("S1").rules(List.of()).build();
        Stock stock = Stock.builder().ticker("AAPL").currentPrice(BigDecimal.valueOf(120)).build();
        HistoricalData historicalData = HistoricalData.builder().ticker("AAPL").candles(List.of()).build();
        when(stockProviderPort.getQuote("AAPL")).thenReturn(stock);
        when(historicalProviderPort.fetchHistoricalData("AAPL")).thenReturn(historicalData);
        when(stockHistoricalService.calculateIndicators(historicalData, 20)).thenReturn(null);

        DeterministicTickerEvaluation result = evaluator.evaluate("AAPL", strategy);

        assertThat(result.isSuitable()).isFalse();
        assertThat(result.getTraceability()).containsExactly("No se pudieron calcular indicadores técnicos para el ticker.");
    }
}
