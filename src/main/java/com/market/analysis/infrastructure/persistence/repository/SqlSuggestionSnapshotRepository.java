package com.market.analysis.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.domain.model.SuggestionSnapshot;
import com.market.analysis.domain.port.out.SuggestionSnapshotRepository;
import com.market.analysis.infrastructure.persistence.mapper.SuggestionSnapshotMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SqlSuggestionSnapshotRepository implements SuggestionSnapshotRepository {

    private final JpaSuggestionSnapshotRepository jpaSuggestionSnapshotRepository;
    private final SuggestionSnapshotMapper suggestionSnapshotMapper;

    @Override
    @Transactional
    public SuggestionSnapshot save(SuggestionSnapshot snapshot) {
        return suggestionSnapshotMapper.toDomain(
                jpaSuggestionSnapshotRepository.save(
                        suggestionSnapshotMapper.toEntity(snapshot)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SuggestionSnapshot> findLatestByStrategyId(Long strategyId) {
        return jpaSuggestionSnapshotRepository.findTopByStrategyIdOrderBySuggestedAtDescIdDesc(strategyId)
                .map(suggestionSnapshotMapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteAllByStrategyId(Long strategyId) {
        jpaSuggestionSnapshotRepository.deleteAllByStrategyId(strategyId);
    }
    
}
