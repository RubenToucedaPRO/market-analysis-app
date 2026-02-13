package com.market.analysis.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.infrastructure.persistence.entity.StockEntity;
import com.market.analysis.infrastructure.persistence.entity.StrategyEvaluationEntity;

/**
 * Mapper for converting between StrategyEvaluation domain model
 * and StrategyEvaluationEntity JPA entity.
 */
@Component
public class StrategyEvaluationMapper {

    /**
     * Converts domain model to JPA entity.
     */
    public StrategyEvaluationEntity toEntity(StrategyEvaluation domain, StockEntity stockEntity) {
        if (domain == null) {
            return null;
        }

        StrategyEvaluationEntity entity = new StrategyEvaluationEntity();
        entity.setId(domain.getId());
        entity.setStock(stockEntity);
        entity.setStrategyName(domain.getStrategyName());
        entity.setCompliant(domain.isCompliant());
        entity.setComplianceRate(domain.getComplianceRate());
        entity.setSummary(domain.getSummary());
        entity.setEvaluatedAt(domain.getEvaluatedAt());
        entity.setPriceAtEvaluation(domain.getPriceAtEvaluation());
        entity.setLatest(domain.isLatest());

        return entity;
    }

    /**
     * Converts JPA entity to domain model.
     */
    public StrategyEvaluation toDomain(StrategyEvaluationEntity entity) {
        if (entity == null) {
            return null;
        }

        return StrategyEvaluation.builder()
                .id(entity.getId())
                .ticker(entity.getStock().getTicker())
                .strategyName(entity.getStrategyName())
                .strategyId(entity.getStock().getStrategyId())
                .compliant(entity.isCompliant())
                .complianceRate(entity.getComplianceRate())
                .summary(entity.getSummary())
                .evaluatedAt(entity.getEvaluatedAt())
                .priceAtEvaluation(entity.getPriceAtEvaluation())
                .isLatest(entity.isLatest())
                .build();
    }
}
