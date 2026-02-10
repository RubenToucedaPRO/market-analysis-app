package com.market.analysis.infrastructure.persistence.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.infrastructure.persistence.entity.ProhibitedTickerEntity;

/**
 * Mapper class to convert between ProhibitedTicker domain model and
 * ProhibitedTickerEntity.
 * This class is responsible for translating the domain model to a format
 * suitable for persistence and vice versa.
 */
@Component
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
        entity.setReason(prohibitedTicker.getReason());
        entity.setCreatedAt(
                prohibitedTicker.getCreatedAt() != null ? prohibitedTicker.getCreatedAt() : LocalDateTime.now());
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

        return ProhibitedTicker.builder()
                .ticker(entity.getTicker())
                .reason(entity.getReason())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
