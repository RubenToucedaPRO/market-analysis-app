package com.market.analysis.infrastructure.persistence.mapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.SuggestedTickerSnapshot;
import com.market.analysis.domain.model.SuggestionSnapshot;
import com.market.analysis.infrastructure.persistence.entity.SuggestedTickerSnapshotEntity;
import com.market.analysis.infrastructure.persistence.entity.SuggestionSnapshotEntity;

@Component
public class SuggestionSnapshotMapper {

    private static final String LINE_SEPARATOR = "\n";

    public SuggestionSnapshotEntity toEntity(SuggestionSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }

        SuggestionSnapshotEntity entity = new SuggestionSnapshotEntity();
        entity.setStrategyId(snapshot.getStrategyId());
        entity.setSuggestedAt(snapshot.getSuggestedAt());
        entity.setAppliedFilters(snapshot.getAppliedFilters());
        entity.setUnmappableRules(serializeList(snapshot.getUnmappableRules()));
        entity.setWarnings(serializeList(snapshot.getWarnings()));
        entity.setSuggestedTickers(snapshot.getSuggestedTickers().stream()
                .map(this::toEntityTicker)
                .toList());
        return entity;
    }

    public SuggestionSnapshot toDomain(SuggestionSnapshotEntity entity) {
        if (entity == null) {
            return null;
        }

        return SuggestionSnapshot.builder()
                .strategyId(entity.getStrategyId())
                .suggestedAt(entity.getSuggestedAt())
                .appliedFilters(entity.getAppliedFilters())
                .unmappableRules(deserializeList(entity.getUnmappableRules()))
                .warnings(deserializeList(entity.getWarnings()))
                .suggestedTickers(Optional.ofNullable(entity.getSuggestedTickers()).orElse(List.of()).stream()
                        .map(this::toDomainTicker)
                        .toList())
                .build();
    }

    private SuggestedTickerSnapshotEntity toEntityTicker(SuggestedTickerSnapshot snapshot) {
        SuggestedTickerSnapshotEntity entity = new SuggestedTickerSnapshotEntity();
        entity.setTicker(snapshot.getTicker());
        entity.setStrategyId(snapshot.getStrategyId());
        entity.setSuggestedAt(snapshot.getSuggestedAt());
        entity.setSuitabilityStatus(snapshot.getSuitabilityStatus());
        entity.setDeterministicMetrics(serializeList(snapshot.getDeterministicMetrics()));
        entity.setTraceability(serializeList(snapshot.getTraceability()));
        return entity;
    }

    private SuggestedTickerSnapshot toDomainTicker(SuggestedTickerSnapshotEntity entity) {
        return SuggestedTickerSnapshot.builder()
                .ticker(entity.getTicker())
                .strategyId(entity.getStrategyId())
                .suggestedAt(entity.getSuggestedAt())
                .suitabilityStatus(entity.getSuitabilityStatus())
                .deterministicMetrics(deserializeList(entity.getDeterministicMetrics()))
                .traceability(deserializeList(entity.getTraceability()))
                .build();
    }

    private String serializeList(List<String> items) {
        return String.join(LINE_SEPARATOR, Optional.ofNullable(items).orElse(List.of()));
    }

    private List<String> deserializeList(String serialized) {
        if (serialized == null || serialized.isBlank()) {
            return List.of();
        }
        return Arrays.stream(serialized.split(LINE_SEPARATOR))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
