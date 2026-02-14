package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;
import com.market.analysis.infrastructure.exception.PersistenceException;
import com.market.analysis.infrastructure.persistence.entity.RuleDefinitionEntity;
import com.market.analysis.infrastructure.persistence.mapper.RuleDefinitionMapper;

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
        try {
            RuleDefinitionEntity entity = mapper.toEntity(ruleDefinition);
            RuleDefinitionEntity savedEntity = jpaRepository.save(entity);
            return mapper.toDomain(savedEntity);
        } catch (DataAccessException e) {
            log.error("Database error saving rule definition: {}", ruleDefinition.getName(), e);
            throw new PersistenceException("Error saving rule definition: " + ruleDefinition.getName(), e);
        }
    }

    @Override
    public Optional<RuleDefinition> findById(Long id) {
        try {
            return jpaRepository.findById(id)
                    .map(mapper::toDomain);
        } catch (DataAccessException e) {
            log.error("Database error finding rule definition by id: {}", id, e);
            throw new PersistenceException("Error finding rule definition by id: " + id, e);
        }
    }

    @Override
    public Optional<RuleDefinition> findByCode(String code) {
        try {
            RuleDefinitionEntity entity = jpaRepository.findByCode(code);
            return Optional.ofNullable(mapper.toDomain(entity));
        } catch (DataAccessException e) {
            log.error("Database error finding rule definition by code: {}", code, e);
            throw new PersistenceException("Error finding rule definition by code: " + code, e);
        }
    }

    @Override
    public List<RuleDefinition> findAll() {
        try {
            return jpaRepository.findAll().stream()
                    .map(mapper::toDomain)
                    .toList();
        } catch (DataAccessException e) {
            log.error("Database error finding all rule definitions", e);
            throw new PersistenceException("Error finding all rule definitions", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            if (strategyRepository.findAll().stream()
                    .anyMatch(strategy -> strategy.getRules().stream()
                            .anyMatch(rule -> rule.getId() != null && rule.getId().equals(id)))) {
                throw new IllegalArgumentException("No se puede eliminar la definición de regla porque está asociada a una estrategia.");
            }
            jpaRepository.deleteById(id);
        } catch (IllegalArgumentException e) {
            // Re-throw business rule violations
            throw e;
        } catch (DataAccessException e) {
            log.error("Database error deleting rule definition by id: {}", id, e);
            throw new PersistenceException("Error deleting rule definition by id: " + id, e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        try {
            return jpaRepository.existsById(id);
        } catch (DataAccessException e) {
            log.error("Database error checking if rule definition exists by id: {}", id, e);
            throw new PersistenceException("Error checking if rule definition exists by id: " + id, e);
        }
    }

    @Override
    public boolean existsByCode(String code) {
        try {
            return jpaRepository.existsByCode(code);
        } catch (DataAccessException e) {
            log.error("Database error checking if rule definition exists by code: {}", code, e);
            throw new PersistenceException("Error checking if rule definition exists by code: " + code, e);
        }
    }
}
