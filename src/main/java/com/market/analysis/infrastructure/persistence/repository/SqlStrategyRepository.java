package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.infrastructure.persistence.entity.StrategyEntity;
import com.market.analysis.infrastructure.persistence.mapper.StrategyMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqlStrategyRepository implements StrategyRepository { // Tu interfaz de dominio

    private final JpaStrategyRepository jpaRepository;
    private final JpaStockDataRepository stockDataRepository;
    private final StrategyMapper mapper;

    @Override
    @Transactional
    public Strategy save(Strategy strategy) {
        log.debug("Saving strategy with ID: {}", strategy.getId());
        StrategyEntity entity = mapper.toEntity(strategy);
        Strategy savedStrategy = mapper.toDomain(jpaRepository.save(entity));
        log.debug("Strategy saved successfully with ID: {}", savedStrategy.getId());
        return savedStrategy;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Strategy> findById(Long id) {
        log.debug("Finding strategy by ID: {}", id);
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Strategy> findByName(String name) {
        log.debug("Finding strategy by name: {}", name);
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .filter(strategy -> strategy.getName().equals(name))
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Strategy> findAll() {
        log.debug("Retrieving all strategies");
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting strategy with ID: {}", id);
        if (stockDataRepository.findAll().stream()
                .anyMatch(stock -> stock.getStrategyId() != null && stock.getStrategyId().equals(id))) {
            throw new IllegalArgumentException("No se puede eliminar la estrategia porque tiene stocks asociados.");
        }
        jpaRepository.deleteById(id);
        log.debug("Strategy deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        log.debug("Checking if strategy exists with ID: {}", id);
        return jpaRepository.existsById(id);
    }

}
