package com.market.analysis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.market.analysis.infrastructure.persistence.entity.StrategyEntity;

public interface JpaStrategyRepository extends JpaRepository<StrategyEntity, Long> {
}
