package com.market.analysis.application.usecase;

import java.util.List;

import com.market.analysis.domain.model.HistoricalData;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.model.TechnicalIndicators;
import com.market.analysis.domain.port.out.HistoricalProviderPort;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.domain.service.EvaluateStrategyService;
import com.market.analysis.domain.service.StockHistoricalService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultDeterministicTickerEvaluator implements DeterministicTickerEvaluator {

    private static final int DEFAULT_INDICATOR_PERIOD = 20;
    private static final String TRACE_QUOTE_NOT_AVAILABLE = "No se pudo obtener cotización para el ticker.";
    private static final String TRACE_HISTORICAL_NOT_AVAILABLE = "No se pudieron obtener datos históricos para el ticker.";
    private static final String TRACE_INDICATORS_NOT_AVAILABLE = "No se pudieron calcular indicadores técnicos para el ticker.";
    private static final String TRACE_EVALUATION_ERROR_PREFIX = "No se pudo evaluar de forma determinista: ";

    private final StockProviderPort stockProviderPort;
    private final HistoricalProviderPort historicalProviderPort;
    private final StockHistoricalService stockHistoricalService;
    private final EvaluateStrategyService evaluateStrategyService;

    @Override
    public DeterministicTickerEvaluation evaluate(String ticker, Strategy strategy) {
        if (ticker == null || ticker.isBlank() || strategy == null) {
            return DeterministicTickerEvaluation.builder()
                    .suitable(false)
                    .traceability(List.of(TRACE_EVALUATION_ERROR_PREFIX + "request inválida"))
                    .build();
        }

        try {
            Stock stock = stockProviderPort.getQuote(ticker);
            if (stock == null) {
                return notSuitable(TRACE_QUOTE_NOT_AVAILABLE);
            }

            HistoricalData historicalData = historicalProviderPort.fetchHistoricalData(ticker);
            if (historicalData == null) {
                return notSuitable(TRACE_HISTORICAL_NOT_AVAILABLE);
            }

            TechnicalIndicators indicators = stockHistoricalService.calculateIndicators(historicalData, DEFAULT_INDICATOR_PERIOD);
            if (indicators == null) {
                return notSuitable(TRACE_INDICATORS_NOT_AVAILABLE);
            }

            stock.applyTechnicalIndicators(indicators);
            StrategyEvaluation evaluation = evaluateStrategyService.evaluateStrategy(strategy, stock);

            return DeterministicTickerEvaluation.builder()
                    .suitable(evaluation != null && evaluation.isCompliant())
                    .traceability(List.of(evaluation != null ? evaluation.getSummary() : TRACE_EVALUATION_ERROR_PREFIX + "sin resultado"))
                    .build();
        } catch (RuntimeException ex) {
            return notSuitable(TRACE_EVALUATION_ERROR_PREFIX + ex.getMessage());
        }
    }

    private DeterministicTickerEvaluation notSuitable(String trace) {
        return DeterministicTickerEvaluation.builder()
                .suitable(false)
                .traceability(List.of(trace))
                .build();
    }

}
