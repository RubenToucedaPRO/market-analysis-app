package com.market.analysis.infrastructure.persistence.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
import com.market.analysis.infrastructure.exception.PersistenceException;
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
        try {
            StockEntity managedStock = jpaStockRepository.findById(stock.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Stock no encontrado con ID: " + stock.getId()));

            StrategyEvaluationEntity savedEntity = jpaRepository
                    .save(entityMapper.toEntity(evaluation, managedStock));
            return entityMapper.toDomain(savedEntity);
        } catch (EntityNotFoundException e) {
            // Re-throw business exceptions
            throw e;
        } catch (DataAccessException e) {
            log.error("Database error saving strategy evaluation for stock id: {}", stock.getId(), e);
            throw new PersistenceException("Error saving strategy evaluation for stock id: " + stock.getId(), e);
        }
    }

}
