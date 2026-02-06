package com.market.analysis.infrastructure.persistence.mapper;

import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.infrastructure.persistence.entity.ProhibitedTickerEntity;

/**
 * Mapper class to convert between ProhibitedTicker domain model and
 * ProhibitedTickerEntity.
 * This class is responsible for translating the domain model to a format
 * suitable for persistence and vice versa.
 */
public class ProhibitedTickerMapper {

    /**
     * Converts a ProhibitedTicker domain model to a ProhibitedTickerEntity.
     * 
     * @param prohibitedTicker
     * @return
     */
    public ProhibitedTickerEntity toEntity(ProhibitedTicker prohibitedTicker) {
        if (prohibitedTicker == null) {
            return null;
        }

        ProhibitedTickerEntity entity = new ProhibitedTickerEntity();
        entity.setTicker(prohibitedTicker.getTicker());
        return entity;
    }

    /**
     * Converts a ProhibitedTickerEntity to a ProhibitedTicker domain model.
     * 
     * @param entity
     * @return
     */
    public ProhibitedTicker toDomain(ProhibitedTickerEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ProhibitedTicker(entity.getTicker());
    }
}
