package com.market.analysis.domain.port.out;

import java.util.Optional;

import com.market.analysis.domain.model.SuggestionSnapshot;

public interface SuggestionSnapshotRepository {

    SuggestionSnapshot save(SuggestionSnapshot snapshot);

    Optional<SuggestionSnapshot> findLatestByStrategyId(Long strategyId);
}
