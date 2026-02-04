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

@Component
@RequiredArgsConstructor
public class SqlStrategyRepository implements StrategyRepository { // Tu interfaz de dominio

    private final JpaStrategyRepository jpaRepository;
    private final StrategyMapper mapper;

    @Override
    @Transactional
    public Strategy save(Strategy strategy) {
        StrategyEntity entity = mapper.toEntity(strategy);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Strategy> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Strategy> findByName(String name) {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .filter(strategy -> strategy.getName().equals(name))
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Strategy> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

}
