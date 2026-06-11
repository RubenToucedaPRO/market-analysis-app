package com.market.analysis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.market.analysis.infrastructure.persistence.entity.SuggestedTickerSnapshotEntity;
public interface JpaSuggestedTickerRepository extends JpaRepository<SuggestedTickerSnapshotEntity, Long> {

    void deleteByStrategyIdAndTicker(Long strategyId,String ticker);
}
