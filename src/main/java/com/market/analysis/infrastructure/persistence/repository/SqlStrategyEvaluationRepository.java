package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
import com.market.analysis.infrastructure.persistence.mapper.StrategyEvaluationMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SQL implementation of StrategyEvaluationRepository.
 * 
 * Adapts the domain repository interface to Spring Data JPA,
 * following Clean Architecture hexagonal pattern.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class SqlStrategyEvaluationRepository implements StrategyEvaluationRepository {

    private final JpaStrategyEvaluationRepository jpaRepository;
    private final StrategyEvaluationMapper mapper;

    @Override
    @Transactional
    public StrategyEvaluation save(StrategyEvaluation evaluation) {
        log.debug("Saving strategy evaluation for ticker: {}, strategyId: {}",
                evaluation.getTicker(), evaluation.getStrategyId());

        var entity = mapper.toEntity(evaluation);
        var savedEntity = jpaRepository.save(entity);

        if (savedEntity.isLatest()) {
            markAsLatestForTickerAndStrategy(
                    savedEntity.getId(),
                    savedEntity.getTicker(),
                    savedEntity.getStrategyId());
        }

        log.debug("Strategy evaluation saved with id: {}", savedEntity.getId());
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<StrategyEvaluation> findLatestByTickerAndStrategyId(String ticker, Long strategyId) {
        log.debug("Finding latest evaluation for ticker: {}, strategyId: {}", ticker, strategyId);
        return jpaRepository
                .findFirstByTickerAndStrategyIdAndLatestTrueOrderByEvaluatedAtDesc(ticker, strategyId)
                .map(mapper::toDomain);
    }

    @Override
    public List<StrategyEvaluation> findByTicker(String ticker) {
        log.debug("Finding all evaluations for ticker: {}", ticker);
        return jpaRepository.findByTickerOrderByEvaluatedAtDesc(ticker)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<StrategyEvaluation> findByStrategyId(Long strategyId) {
        log.debug("Finding all evaluations for strategyId: {}", strategyId);
        return jpaRepository.findByStrategyIdOrderByEvaluatedAtDesc(strategyId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<StrategyEvaluation> findById(Long id) {
        log.debug("Finding evaluation by id: {}", id);
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting evaluation by id: {}", id);
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void markAsLatestForTickerAndStrategy(Long evaluationId, String ticker, Long strategyId) {
        log.debug("Marking evaluation {} as latest for ticker: {}, strategyId: {}",
                evaluationId, ticker, strategyId);
        jpaRepository.updateLatestToFalse(ticker, strategyId, evaluationId);
    }
}
