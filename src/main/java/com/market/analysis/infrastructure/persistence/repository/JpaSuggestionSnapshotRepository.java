package com.market.analysis.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.market.analysis.infrastructure.persistence.entity.SuggestionSnapshotEntity;

@Repository
public interface JpaSuggestionSnapshotRepository extends JpaRepository<SuggestionSnapshotEntity, Long> {

    Optional<SuggestionSnapshotEntity> findTopByStrategyIdOrderBySuggestedAtDescIdDesc(Long strategyId);
}
