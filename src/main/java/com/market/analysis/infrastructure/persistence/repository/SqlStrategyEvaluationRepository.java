package com.market.analysis.infrastructure.persistence.repository;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
import com.market.analysis.infrastructure.persistence.entity.StockEntity;
import com.market.analysis.infrastructure.persistence.entity.StrategyEvaluationEntity;
import com.market.analysis.infrastructure.persistence.mapper.StrategyEvaluationMapper;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SQL implementation of StrategyEvaluationRepository.
 * 
 * Adapts the domain repository interface to Spring Data JPA,
 * following Clean Architecture hexagonal pattern.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SqlStrategyEvaluationRepository implements StrategyEvaluationRepository {

    private final JpaStrategyEvaluationRepository jpaRepository;
    private final StrategyEvaluationMapper entityMapper;
    private final JpaStockDataRepository jpaStockRepository;

    @Override
    @Transactional
    public StrategyEvaluation save(StrategyEvaluation evaluation, Stock stock) {
        log.debug("Saving strategy evaluation for ticker: {}, strategyId: {}", evaluation.getTicker(), evaluation.getStrategyId());

        StockEntity managedStock = jpaStockRepository.findById(stock.getId())
                .orElseThrow(() -> new EntityNotFoundException("Stock no encontrado con ID: " + stock.getId()));

        StrategyEvaluationEntity savedEntity = jpaRepository
                .save(entityMapper.toEntity(evaluation, managedStock));
        StrategyEvaluation saved = entityMapper.toDomain(savedEntity);
        log.debug("Strategy evaluation saved successfully for ticker: {}", saved.getTicker());
        return saved;
    }

}
