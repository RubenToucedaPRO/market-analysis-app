package com.market.analysis.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyObjective;
import com.market.analysis.infrastructure.persistence.entity.RuleEntity;
import com.market.analysis.infrastructure.persistence.entity.StrategyEntity;

@Component
public class StrategyMapper {

    private final RuleMapper ruleMapper;

    StrategyMapper(RuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
    }
    public Strategy toDomain(StrategyEntity entity) {
        if (entity == null) return null;

        // Build objective if present
        StrategyObjective objective = null;
        if (entity.getTargetType() != null && entity.getStopLossType() != null) {
            objective = StrategyObjective.builder()
                    .targetType(mapObjectiveType(entity.getTargetType()))
                    .targetValue(entity.getTargetValue())
                    .stopLossType(mapObjectiveType(entity.getStopLossType()))
                    .stopLossValue(entity.getStopLossValue())
                    .capitalToRisk(entity.getCapitalToRisk())
                    .description(entity.getObjectiveDescription())
                    .build();
        }

        return Strategy.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .rules(entity.getRules() != null 
                        ? entity.getRules().stream()
                                .map(ruleMapper::toDomain)
                                .toList()
                        : java.util.List.of())
                .objective(objective)
                .build();
    }

    public StrategyEntity toEntity(Strategy domain) {
        if (domain == null) return null;

        StrategyEntity entity = new StrategyEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        
        if (domain.getRules() != null) {
            domain.getRules().forEach(rule -> {
                RuleEntity ruleEntity = ruleMapper.toEntity(rule);
                entity.addRule(ruleEntity); // Usamos el helper que creamos en StrategyEntity
            });
        }

        // Map objective if present
        if (domain.hasObjective()) {
            StrategyObjective objective = domain.getObjective();
            entity.setTargetType(mapObjectiveType(objective.getTargetType()));
            entity.setTargetValue(objective.getTargetValue());
            entity.setStopLossType(mapObjectiveType(objective.getStopLossType()));
            entity.setStopLossValue(objective.getStopLossValue());
            entity.setCapitalToRisk(objective.getCapitalToRisk());
            entity.setObjectiveDescription(objective.getDescription());
        }
        
        return entity;
    }

    private StrategyObjective.ObjectiveType mapObjectiveType(StrategyEntity.ObjectiveType entityType) {
        if (entityType == null) return null;
        return StrategyObjective.ObjectiveType.valueOf(entityType.name());
    }

    private StrategyEntity.ObjectiveType mapObjectiveType(StrategyObjective.ObjectiveType domainType) {
        if (domainType == null) return null;
        return StrategyEntity.ObjectiveType.valueOf(domainType.name());
    }
}