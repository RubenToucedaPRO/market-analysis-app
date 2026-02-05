package com.market.analysis.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.Rule;
import com.market.analysis.infrastructure.persistence.entity.RuleEntity;

@Component
public class RuleMapper {

    public Rule toDomain(RuleEntity entity) {
        if (entity == null)
            return null;

        return Rule.builder()
                .id(entity.getId())
                .name(entity.getName())
                .subjectCode(entity.getSubjectCode())
                .subjectParam(entity.getSubjectParam())
                .operator(entity.getOperator())
                .targetCode(entity.getTargetCode())
                .targetParam(entity.getTargetParam())
                .description(entity.getDescription())
                .build();
    }

    public RuleEntity toEntity(Rule domain) {
        if (domain == null)
            return null;

        RuleEntity entity = new RuleEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setSubjectCode(domain.getSubjectCode());
        entity.setSubjectParam(domain.getSubjectParam());
        entity.setOperator(domain.getOperator());
        entity.setTargetCode(domain.getTargetCode());
        entity.setTargetParam(domain.getTargetParam());
        entity.setDescription(domain.getDescription());
        return entity;
    }
}
