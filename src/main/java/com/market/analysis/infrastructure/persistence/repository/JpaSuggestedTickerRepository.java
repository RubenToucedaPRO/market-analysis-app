package com.market.analysis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.market.analysis.infrastructure.persistence.entity.SuggestedTickerSnapshotEntity;

@Repository
public interface JpaSuggestedTickerRepository extends JpaRepository<SuggestedTickerSnapshotEntity, Long> {

    void deleteByStrategyIdAndTicker(Long strategyId,String ticker);
}
