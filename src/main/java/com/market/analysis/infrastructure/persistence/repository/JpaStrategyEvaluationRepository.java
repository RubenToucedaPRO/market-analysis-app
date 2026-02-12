package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.market.analysis.infrastructure.persistence.entity.StrategyEvaluationEntity;

/**
 * Spring Data JPA repository for StrategyEvaluationEntity.
 * Provides automatic CRUD and query operations.
 */
@Repository
public interface JpaStrategyEvaluationRepository extends JpaRepository<StrategyEvaluationEntity, Long> {

    /**
     * Finds the most recent evaluation for a ticker and strategy.
     */
    Optional<StrategyEvaluationEntity> findFirstByTickerAndStrategyIdAndLatestTrueOrderByEvaluatedAtDesc(
            String ticker, Long strategyId);

    /**
     * Finds all evaluations for a specific ticker, ordered by timestamp descending.
     */
    List<StrategyEvaluationEntity> findByTickerOrderByEvaluatedAtDesc(String ticker);

    /**
     * Finds all evaluations for a specific strategy, ordered by timestamp
     * descending.
     */
    List<StrategyEvaluationEntity> findByStrategyIdOrderByEvaluatedAtDesc(Long strategyId);

    /**
     * Updates the latest flag for all evaluations except the given one
     * for the same ticker and strategy combination.
     */
    @Modifying
    @Query("UPDATE StrategyEvaluationEntity e SET e.latest = false " +
            "WHERE e.ticker = :ticker AND e.strategyId = :strategyId AND e.id != :evaluationId")
    void updateLatestToFalse(@Param("ticker") String ticker,
            @Param("strategyId") Long strategyId,
            @Param("evaluationId") Long evaluationId);
}
