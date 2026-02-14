package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;
import com.market.analysis.infrastructure.persistence.entity.RuleDefinitionEntity;
import com.market.analysis.infrastructure.persistence.mapper.RuleDefinitionMapper;

import lombok.RequiredArgsConstructor;

/**
 * SQL implementation of the RuleDefinitionRepository port.
 * Adapts between the domain RuleDefinition and the persistence layer.
 */
@Component
@RequiredArgsConstructor
public class SqlRuleDefinitionRepository implements RuleDefinitionRepository {

    private final JpaRuleDefinitionRepository jpaRepository;
    private final JpaStrategyRepository strategyRepository; // Para verificar dependencias antes de eliminar
    private final RuleDefinitionMapper mapper;

    @Override
    public RuleDefinition save(RuleDefinition ruleDefinition) {
        RuleDefinitionEntity entity = mapper.toEntity(ruleDefinition);
        RuleDefinitionEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<RuleDefinition> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<RuleDefinition> findByCode(String code) {
        RuleDefinitionEntity entity = jpaRepository.findByCode(code);
        return Optional.ofNullable(mapper.toDomain(entity));
    }

    @Override
    public List<RuleDefinition> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        if (strategyRepository.findAll().stream()
                .anyMatch(strategy -> strategy.getRules().stream()
                        .anyMatch(rule -> rule.getId() != null && rule.getId().equals(id)))) {
            throw new IllegalArgumentException("No se puede eliminar la definición de regla porque está asociada a una estrategia.");
        }
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }
}
