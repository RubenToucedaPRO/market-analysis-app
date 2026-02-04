package com.market.analysis.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.Strategy;
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

        return Strategy.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .rules(entity.getRules().stream()
                        .map(ruleMapper::toDomain)
                        .toList())
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
        
        return entity;
    }
}