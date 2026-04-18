package com.market.analysis.application.usecase;

import com.market.analysis.domain.model.Strategy;

/**
 * Internal deterministic pipeline for evaluating if a ticker is suitable for a strategy.
 */
public interface DeterministicTickerEvaluator {

    DeterministicTickerEvaluation evaluate(String ticker, Strategy strategy);
}
