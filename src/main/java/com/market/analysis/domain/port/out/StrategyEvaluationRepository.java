package com.market.analysis.domain.port.out;

import java.util.List;
import java.util.Optional;

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
    StrategyEvaluation save(StrategyEvaluation evaluation);

    /**
     * Retrieves the most recent evaluation for a given ticker and strategy.
     *
     * @param ticker     the ticker symbol
     * @param strategyId the strategy ID
     * @return Optional containing the latest evaluation if exists
     */
    Optional<StrategyEvaluation> findLatestByTickerAndStrategyId(String ticker, Long strategyId);

    /**
     * Retrieves all evaluations for a specific ticker.
     *
     * @param ticker the ticker symbol
     * @return list of all evaluations for the ticker
     */
    List<StrategyEvaluation> findByTicker(String ticker);

    /**
     * Retrieves all evaluations for a specific strategy.
     *
     * @param strategyId the strategy ID
     * @return list of all evaluations for the strategy
     */
    List<StrategyEvaluation> findByStrategyId(Long strategyId);

    /**
     * Retrieves a specific evaluation by ID.
     *
     * @param id the evaluation ID
     * @return Optional containing the evaluation if found
     */
    Optional<StrategyEvaluation> findById(Long id);

    /**
     * Deletes an evaluation by ID.
     *
     * @param id the evaluation ID
     */
    void deleteById(Long id);

    /**
     * Updates the isLatest flag for evaluations.
     * Sets isLatest to false for all evaluations except the given ID
     * for the same ticker and strategy.
     *
     * @param evaluationId the ID of the evaluation to mark as latest
     * @param ticker       the ticker symbol
     * @param strategyId   the strategy ID
     */
    void markAsLatestForTickerAndStrategy(Long evaluationId, String ticker, Long strategyId);
}
