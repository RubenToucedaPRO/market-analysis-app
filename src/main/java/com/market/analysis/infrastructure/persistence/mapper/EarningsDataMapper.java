package com.market.analysis.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.EarningsData;
import com.market.analysis.infrastructure.persistence.entity.EarningsDataEntity;

/**
 * Mapper class to convert between EarningsData domain model and
 * EarningsDataEntity.
 * This class is responsible for translating the domain model to the persistence
 * entity and vice versa.
 * It is part of the infrastructure layer, ensuring separation of concerns
 * between the domain and persistence layers
 */
@Component
public class EarningsDataMapper {
    /**
     * Converts an EarningsData domain model to an EarningsDataEntity.
     *
     * @param earningsData the domain model to convert
     * @return the corresponding EarningsDataEntity
     */
    public EarningsDataEntity toEntity(EarningsData earningsData) {
        if (earningsData == null) {
            return null;
        }

        EarningsDataEntity entity = new EarningsDataEntity();
        entity.setTicker(earningsData.getTicker());
        entity.setDate(earningsData.getDate());

        return entity;
    }

    /**
     * Converts an EarningsDataEntity to an EarningsData domain model.
     *
     * @param entity the entity to convert
     * @return the corresponding EarningsData domain model
     */
    public EarningsData toDomain(EarningsDataEntity entity) {
        if (entity == null) {
            return null;
        }

        return EarningsData.builder()
                .ticker(entity.getTicker())
                .date(entity.getDate())
                .build();
    }

}
