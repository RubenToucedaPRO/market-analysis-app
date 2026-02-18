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
        if (entity.getTargetPrice() != null && entity.getStopLossPrice() != null && entity.getPositionType() != null) {
            objective = StrategyObjective.builder()
                    .targetPrice(entity.getTargetPrice())
                    .stopLossPrice(entity.getStopLossPrice())
                    .positionType(mapPositionType(entity.getPositionType()))
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
            entity.setTargetPrice(objective.getTargetPrice());
            entity.setStopLossPrice(objective.getStopLossPrice());
            entity.setPositionType(mapPositionType(objective.getPositionType()));
            entity.setObjectiveDescription(objective.getDescription());
        }
        
        return entity;
    }

    private StrategyObjective.PositionType mapPositionType(StrategyEntity.PositionType entityType) {
        if (entityType == null) return null;
        return StrategyObjective.PositionType.valueOf(entityType.name());
    }

    private StrategyEntity.PositionType mapPositionType(StrategyObjective.PositionType domainType) {
        if (domainType == null) return null;
        return StrategyEntity.PositionType.valueOf(domainType.name());
    }
}