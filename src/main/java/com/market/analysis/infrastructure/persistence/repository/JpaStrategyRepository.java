package com.market.analysis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.market.analysis.infrastructure.persistence.entity.StrategyEntity;

@Repository
public interface JpaStrategyRepository extends JpaRepository<StrategyEntity, Long> {

}
