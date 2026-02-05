package com.market.analysis.domain.port.in;

import com.market.analysis.domain.model.AnalysisResult;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.TickerData;

/**
 * Input port (use case interface) for evaluating trading strategies.
 * Defines the contract for strategy evaluation without binding to specific implementations.
 * 
 * This interface follows Clean Architecture principles, serving as the entry point
 * for the application layer to execute strategy evaluation use cases.
 * 
 * No Spring or framework annotations should be present here to maintain
 * technology independence in the domain layer.
 */
public interface EvaluateStrategyUseCase {

    /**
     * Evaluates a trading strategy against the provided ticker data.
     * 
     * @param strategy the trading strategy to evaluate
     * @param tickerData the market data for the ticker to analyze
     * @return AnalysisResult containing evaluation results, rule outcomes, and metrics
     * @throws IllegalArgumentException if strategy or tickerData is null
     */
    AnalysisResult evaluateStrategy(Strategy strategy, TickerData tickerData);
}
