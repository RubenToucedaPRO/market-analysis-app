package com.market.analysis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.market.analysis.infrastructure.persistence.entity.StrategyEvaluationEntity;

/**
 * Spring Data JPA repository for StrategyEvaluationEntity.
 * Provides automatic CRUD and query operations.
 */
public interface JpaStrategyEvaluationRepository extends JpaRepository<StrategyEvaluationEntity, Long> {

}
