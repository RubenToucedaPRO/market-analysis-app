package com.market.analysis.unit.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.SuggestedTickerSnapshot;
import com.market.analysis.domain.model.SuggestionSnapshot;
import com.market.analysis.infrastructure.persistence.entity.SuggestedTickerSnapshotEntity;
import com.market.analysis.infrastructure.persistence.entity.SuggestionSnapshotEntity;
import com.market.analysis.infrastructure.persistence.mapper.SuggestionSnapshotMapper;

@DisplayName("SuggestionSnapshotMapper Unit Tests")
class SuggestionSnapshotMapperTest {

    private final SuggestionSnapshotMapper mapper = new SuggestionSnapshotMapper();

    @Test
    @DisplayName("Should map per-ticker onboarding fields from domain to entity")
    void shouldMapOnboardingFieldsToEntity() {
        Instant suggestedAt = Instant.parse("2026-04-20T08:00:00Z");
        SuggestionSnapshot snapshot = SuggestionSnapshot.builder()
                .strategyId(7L)
                .suggestedAt(suggestedAt)
                .appliedFilters("ta_sma20_pa")
                .suggestedTickers(List.of(
                        SuggestedTickerSnapshot.builder()
                                .ticker("AAPL")
                                .strategyId(7L)
                                .suggestedAt(suggestedAt)
                                .suitabilityStatus("APTO")
                                .deterministicMetrics(List.of("SMA20=123.45"))
                                .traceability(List.of("Cumple"))
                                .build()))
                .build();

        SuggestionSnapshotEntity entity = mapper.toEntity(snapshot);

        assertNotNull(entity);
        assertEquals(1, entity.getSuggestedTickers().size());
        SuggestedTickerSnapshotEntity tickerEntity = entity.getSuggestedTickers().get(0);
        assertEquals(7L, tickerEntity.getStrategyId());
        assertEquals(suggestedAt, tickerEntity.getSuggestedAt());
        assertEquals("SMA20=123.45", tickerEntity.getDeterministicMetrics());
    }

    @Test
    @DisplayName("Should map per-ticker onboarding fields from entity to domain")
    void shouldMapOnboardingFieldsToDomain() {
        Instant suggestedAt = Instant.parse("2026-04-20T08:00:00Z");
        SuggestedTickerSnapshotEntity tickerEntity = new SuggestedTickerSnapshotEntity();
        tickerEntity.setTicker("AAPL");
        tickerEntity.setStrategyId(7L);
        tickerEntity.setSuggestedAt(suggestedAt);
        tickerEntity.setSuitabilityStatus("APTO");
        tickerEntity.setDeterministicMetrics("SMA20=123.45\nSMA50=120.10");
        tickerEntity.setTraceability("Cumple");

        SuggestionSnapshotEntity entity = new SuggestionSnapshotEntity();
        entity.setStrategyId(7L);
        entity.setSuggestedAt(suggestedAt);
        entity.setSuggestedTickers(List.of(tickerEntity));

        SuggestionSnapshot domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(1, domain.getSuggestedTickers().size());
        SuggestedTickerSnapshot ticker = domain.getSuggestedTickers().get(0);
        assertEquals(7L, ticker.getStrategyId());
        assertEquals(suggestedAt, ticker.getSuggestedAt());
        assertEquals(List.of("SMA20=123.45", "SMA50=120.10"), ticker.getDeterministicMetrics());
    }
}
