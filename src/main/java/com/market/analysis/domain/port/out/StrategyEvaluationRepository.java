package com.market.analysis.domain.port.out;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyEvaluation;

/**
 * Output port (repository interface) for persisting and retrieving strategy
 * evaluations.
 * 
 * Defines the contract for strategy evaluation persistence without binding
 * to specific database implementations. Follows Clean Architecture principles.
 * 
 * No Spring or framework annotations in this domain interface.
 */
public interface StrategyEvaluationRepository {

    /**
     * Persists a strategy evaluation record.
     *
     * @param evaluation the evaluation to persist
     * @return the persisted evaluation with ID set
     */
    StrategyEvaluation save(StrategyEvaluation evaluation, Stock stock);
}
