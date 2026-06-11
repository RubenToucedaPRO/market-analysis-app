package com.market.analysis.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.market.analysis.infrastructure.persistence.entity.SuggestionSnapshotEntity;
public interface JpaSuggestionSnapshotRepository extends JpaRepository<SuggestionSnapshotEntity, Long> {

    Optional<SuggestionSnapshotEntity> findTopByStrategyIdOrderBySuggestedAtDescIdDesc(Long strategyId);

    void deleteAllByStrategyId(Long strategyId);
}
