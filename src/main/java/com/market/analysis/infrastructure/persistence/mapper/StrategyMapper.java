package com.market.analysis.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.ObjectiveType;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyObjective;
import com.market.analysis.infrastructure.persistence.entity.RuleEntity;
import com.market.analysis.infrastructure.persistence.entity.StrategyEntity;
import com.market.analysis.infrastructure.persistence.entity.StrategyObjectiveEntity;

@Component
public class StrategyMapper {

    private final RuleMapper ruleMapper;

    StrategyMapper(RuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
    }

    public Strategy toDomain(StrategyEntity entity) {
        if (entity == null) return null;

        return Strategy.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .rules(entity.getRules() != null
                        ? entity.getRules().stream()
                                .map(ruleMapper::toDomain)
                                .toList()
                        : java.util.List.of())
                .objective(toObjectiveDomain(entity.getObjective()))
                .build();
    }

    public StrategyEntity toEntity(Strategy domain) {
        if (domain == null) return null;

        StrategyEntity entity = new StrategyEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setObjective(toObjectiveEntity(domain.getObjective()));

        if (domain.getRules() != null) {
            domain.getRules().forEach(rule -> {
                RuleEntity ruleEntity = ruleMapper.toEntity(rule);
                entity.addRule(ruleEntity);
            });
        }

        return entity;
    }

    private StrategyObjective toObjectiveDomain(StrategyObjectiveEntity objectiveEntity) {
        if (objectiveEntity == null) return null;

        return StrategyObjective.builder()
                .targetType(objectiveEntity.getTargetType() != null
                        ? ObjectiveType.valueOf(objectiveEntity.getTargetType()) : null)
                .targetValue(objectiveEntity.getTargetValue())
                .stopLossType(objectiveEntity.getStopLossType() != null
                        ? ObjectiveType.valueOf(objectiveEntity.getStopLossType()) : null)
                .stopLossValue(objectiveEntity.getStopLossValue())
                .capitalToRisk(objectiveEntity.getCapitalToRisk())
                .description(objectiveEntity.getDescription())
                .build();
    }

    private StrategyObjectiveEntity toObjectiveEntity(StrategyObjective objective) {
        if (objective == null) return null;

        StrategyObjectiveEntity entity = new StrategyObjectiveEntity();
        entity.setTargetType(objective.getTargetType() != null
                ? objective.getTargetType().name() : null);
        entity.setTargetValue(objective.getTargetValue());
        entity.setStopLossType(objective.getStopLossType() != null
                ? objective.getStopLossType().name() : null);
        entity.setStopLossValue(objective.getStopLossValue());
        entity.setCapitalToRisk(objective.getCapitalToRisk());
        entity.setDescription(objective.getDescription());
        return entity;
    }
}
