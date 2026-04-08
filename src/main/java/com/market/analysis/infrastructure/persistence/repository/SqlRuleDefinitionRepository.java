package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;
import com.market.analysis.infrastructure.persistence.entity.RuleDefinitionEntity;
import com.market.analysis.infrastructure.persistence.mapper.RuleDefinitionMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SQL implementation of the RuleDefinitionRepository port.
 * Adapts between the domain RuleDefinition and the persistence layer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SqlRuleDefinitionRepository implements RuleDefinitionRepository {

    private final JpaRuleDefinitionRepository jpaRepository;
    private final JpaStrategyRepository strategyRepository; // Para verificar dependencias antes de eliminar
    private final RuleDefinitionMapper mapper;

    @Override
    public RuleDefinition save(RuleDefinition ruleDefinition) {
        log.debug("Saving rule definition with code: {}", ruleDefinition.getCode());
        RuleDefinitionEntity entity = mapper.toEntity(ruleDefinition);
        RuleDefinitionEntity savedEntity = jpaRepository.save(entity);
        RuleDefinition saved = mapper.toDomain(savedEntity);
        log.debug("Rule definition saved successfully with ID: {}", saved.getId());
        return saved;
    }

    @Override
    public Optional<RuleDefinition> findById(Long id) {
        log.debug("Finding rule definition by ID: {}", id);
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<RuleDefinition> findByCode(String code) {
        log.debug("Finding rule definition by code: {}", code);
        RuleDefinitionEntity entity = jpaRepository.findByCode(code);
        return Optional.ofNullable(mapper.toDomain(entity));
    }

    @Override
    public List<RuleDefinition> findAll() {
        log.debug("Retrieving all rule definitions");
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting rule definition with ID: {}", id);
        if (strategyRepository.findAll().stream()
                .anyMatch(strategy -> strategy.getRules().stream()
                        .anyMatch(rule -> rule.getId() != null && rule.getId().equals(id)))) {
            throw new IllegalArgumentException("No se puede eliminar la definición de regla porque está asociada a una estrategia.");
        }
        jpaRepository.deleteById(id);
        log.debug("Rule definition deleted successfully with ID: {}", id);
    }

    @Override
    public boolean existsById(Long id) {
        log.debug("Checking if rule definition exists with ID: {}", id);
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        log.debug("Checking if rule definition exists with code: {}", code);
        return jpaRepository.existsByCode(code);
    }
}
