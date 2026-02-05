package com.market.analysis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.market.analysis.infrastructure.persistence.entity.RuleDefinitionEntity;

/**
 * Spring Data JPA repository for RuleDefinitionEntity.
 * Provides standard CRUD operations through JPA.
 */
@Repository
public interface JpaRuleDefinitionRepository extends JpaRepository<RuleDefinitionEntity, Long> {
    
    /**
     * Finds a rule definition by its code.
     * 
     * @param code the rule definition code
     * @return the entity if found, null otherwise
     */
    RuleDefinitionEntity findByCode(String code);

    /**
     * Checks if a rule definition exists by its code.
     * 
     * @param code the rule definition code
     * @return true if exists, false otherwise
     */
    boolean existsByCode(String code);
}
