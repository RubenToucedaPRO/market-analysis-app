package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.infrastructure.exception.PersistenceException;
import com.market.analysis.infrastructure.persistence.entity.StrategyEntity;
import com.market.analysis.infrastructure.persistence.mapper.StrategyMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqlStrategyRepository implements StrategyRepository {

    private final JpaStrategyRepository jpaRepository;
    private final JpaStockDataRepository stockDataRepository;
    private final StrategyMapper mapper;

    @Override
    @Transactional
    public Strategy save(Strategy strategy) {
        try {
            StrategyEntity entity = mapper.toEntity(strategy);
            return mapper.toDomain(jpaRepository.save(entity));
        } catch (DataAccessException e) {
            log.error("Database error saving strategy: {}", strategy.getName(), e);
            throw new PersistenceException("Error saving strategy: " + strategy.getName(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Strategy> findById(Long id) {
        try {
            return jpaRepository.findById(id)
                    .map(mapper::toDomain);
        } catch (DataAccessException e) {
            log.error("Database error finding strategy by id: {}", id, e);
            throw new PersistenceException("Error finding strategy by id: " + id, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Strategy> findByName(String name) {
        try {
            return jpaRepository.findAll().stream()
                    .map(mapper::toDomain)
                    .filter(strategy -> strategy.getName().equals(name))
                    .findFirst();
        } catch (DataAccessException e) {
            log.error("Database error finding strategy by name: {}", name, e);
            throw new PersistenceException("Error finding strategy by name: " + name, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Strategy> findAll() {
        try {
            return jpaRepository.findAll().stream()
                    .map(mapper::toDomain)
                    .toList();
        } catch (DataAccessException e) {
            log.error("Database error finding all strategies", e);
            throw new PersistenceException("Error finding all strategies", e);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        try {
            if (stockDataRepository.findAll().stream()
                    .anyMatch(stock -> stock.getStrategyId() != null && stock.getStrategyId().equals(id))) {
                throw new IllegalArgumentException("No se puede eliminar la estrategia porque tiene stocks asociados.");
            }
            jpaRepository.deleteById(id);
        } catch (IllegalArgumentException e) {
            // Re-throw business rule violations
            throw e;
        } catch (DataAccessException e) {
            log.error("Database error deleting strategy by id: {}", id, e);
            throw new PersistenceException("Error deleting strategy by id: " + id, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        try {
            return jpaRepository.existsById(id);
        } catch (DataAccessException e) {
            log.error("Database error checking if strategy exists by id: {}", id, e);
            throw new PersistenceException("Error checking if strategy exists by id: " + id, e);
        }
    }

}
