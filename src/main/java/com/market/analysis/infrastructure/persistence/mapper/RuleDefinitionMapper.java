package com.market.analysis.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.infrastructure.persistence.entity.RuleDefinitionEntity;

/**
 * Mapper to convert between RuleDefinition domain model and RuleDefinitionEntity.
 * Part of the infrastructure layer, handling translation between domain and persistence.
 */
@Component
public class RuleDefinitionMapper {

    /**
     * Converts a RuleDefinition domain model to a RuleDefinitionEntity.
     * 
     * @param ruleDefinition the domain model
     * @return the entity
     */
    public RuleDefinitionEntity toEntity(RuleDefinition ruleDefinition) {
        if (ruleDefinition == null) {
            return null;
        }
        
        RuleDefinitionEntity entity = new RuleDefinitionEntity();
        entity.setId(ruleDefinition.getId());
        entity.setCode(ruleDefinition.getCode());
        entity.setName(ruleDefinition.getName());
        entity.setRequiresParam(ruleDefinition.isRequiresParam());
        entity.setDescription(ruleDefinition.getDescription());
        
        return entity;
    }

    /**
     * Converts a RuleDefinitionEntity to a RuleDefinition domain model.
     * 
     * @param entity the entity
     * @return the domain model
     */
    public RuleDefinition toDomain(RuleDefinitionEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return RuleDefinition.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .requiresParam(entity.isRequiresParam())
                .description(entity.getDescription())
                .build();
    }
}
