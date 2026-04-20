package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.SuggestionSnapshot;
import com.market.analysis.infrastructure.persistence.entity.SuggestionSnapshotEntity;
import com.market.analysis.infrastructure.persistence.mapper.SuggestionSnapshotMapper;
import com.market.analysis.infrastructure.persistence.repository.JpaSuggestionSnapshotRepository;
import com.market.analysis.infrastructure.persistence.repository.SqlSuggestionSnapshotRepository;

@DisplayName("SqlSuggestionSnapshotRepository Unit Tests")
@ExtendWith(MockitoExtension.class)
class SqlSuggestionSnapshotRepositoryTest {

    @Mock
    private JpaSuggestionSnapshotRepository jpaSuggestionSnapshotRepository;

    @Mock
    private SuggestionSnapshotMapper suggestionSnapshotMapper;

    @InjectMocks
    private SqlSuggestionSnapshotRepository repository;

    @Test
    @DisplayName("Should save snapshot")
    void shouldSaveSnapshot() {
        SuggestionSnapshot snapshot = SuggestionSnapshot.builder()
                .strategyId(10L)
                .suggestedAt(Instant.parse("2026-04-20T10:00:00Z"))
                .build();
        SuggestionSnapshotEntity entity = new SuggestionSnapshotEntity();
        SuggestionSnapshot savedDomain = SuggestionSnapshot.builder()
                .strategyId(10L)
                .suggestedAt(Instant.parse("2026-04-20T10:00:00Z"))
                .build();

        when(suggestionSnapshotMapper.toEntity(snapshot)).thenReturn(entity);
        when(jpaSuggestionSnapshotRepository.save(entity)).thenReturn(entity);
        when(suggestionSnapshotMapper.toDomain(entity)).thenReturn(savedDomain);

        SuggestionSnapshot result = repository.save(snapshot);

        assertThat(result).isEqualTo(savedDomain);
        verify(suggestionSnapshotMapper, times(1)).toEntity(snapshot);
        verify(jpaSuggestionSnapshotRepository, times(1)).save(entity);
        verify(suggestionSnapshotMapper, times(1)).toDomain(entity);
    }

    @Test
    @DisplayName("Should find latest snapshot by strategy id")
    void shouldFindLatestSnapshotByStrategyId() {
        Long strategyId = 10L;
        SuggestionSnapshotEntity entity = new SuggestionSnapshotEntity();
        SuggestionSnapshot domain = SuggestionSnapshot.builder()
                .strategyId(strategyId)
                .suggestedAt(Instant.parse("2026-04-20T10:00:00Z"))
                .build();

        when(jpaSuggestionSnapshotRepository.findTopByStrategyIdOrderBySuggestedAtDescIdDesc(strategyId))
                .thenReturn(Optional.of(entity));
        when(suggestionSnapshotMapper.toDomain(entity)).thenReturn(domain);

        Optional<SuggestionSnapshot> result = repository.findLatestByStrategyId(strategyId);

        assertThat(result).contains(domain);
        verify(jpaSuggestionSnapshotRepository, times(1))
                .findTopByStrategyIdOrderBySuggestedAtDescIdDesc(strategyId);
        verify(suggestionSnapshotMapper, times(1)).toDomain(entity);
    }

    @Test
    @DisplayName("Should return empty when latest snapshot does not exist")
    void shouldReturnEmptyWhenLatestSnapshotDoesNotExist() {
        Long strategyId = 10L;
        when(jpaSuggestionSnapshotRepository.findTopByStrategyIdOrderBySuggestedAtDescIdDesc(strategyId))
                .thenReturn(Optional.empty());

        Optional<SuggestionSnapshot> result = repository.findLatestByStrategyId(strategyId);

        assertThat(result).isEmpty();
        verify(jpaSuggestionSnapshotRepository, times(1))
                .findTopByStrategyIdOrderBySuggestedAtDescIdDesc(strategyId);
        verify(suggestionSnapshotMapper, times(0)).toDomain(any());
    }
}
