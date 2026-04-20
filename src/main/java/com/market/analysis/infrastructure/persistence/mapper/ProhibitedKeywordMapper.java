package com.market.analysis.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.ProhibitedKeyword;
import com.market.analysis.infrastructure.persistence.entity.ProhibitedKeywordEntity;

@Component
public class ProhibitedKeywordMapper {

    public ProhibitedKeywordEntity toEntity(ProhibitedKeyword prohibitedKeyword) {
        if (prohibitedKeyword == null) {
            return null;
        }

        ProhibitedKeywordEntity entity = new ProhibitedKeywordEntity();
        entity.setKeyword(prohibitedKeyword.getKeyword());
        entity.setActive(prohibitedKeyword.isActive());
        entity.setCreatedAt(prohibitedKeyword.getCreatedAt());
        entity.setUpdatedAt(prohibitedKeyword.getUpdatedAt());
        return entity;
    }

    public ProhibitedKeyword toDomain(ProhibitedKeywordEntity entity) {
        if (entity == null) {
            return null;
        }

        return ProhibitedKeyword.builder()
                .keyword(entity.getKeyword())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
