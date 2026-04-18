package com.market.analysis.application.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.application.dto.ProhibitedKeywordDTO;
import com.market.analysis.domain.model.ProhibitedKeyword;

@Component
public class ProhibitedKeywordDTOMapper {

    public ProhibitedKeywordDTO toDTO(ProhibitedKeyword prohibitedKeyword) {
        if (prohibitedKeyword == null) {
            return null;
        }

        return ProhibitedKeywordDTO.builder()
                .keyword(prohibitedKeyword.getKeyword())
                .active(prohibitedKeyword.isActive())
                .createdAt(prohibitedKeyword.getCreatedAt())
                .updatedAt(prohibitedKeyword.getUpdatedAt())
                .build();
    }

    public ProhibitedKeyword toDomain(ProhibitedKeywordDTO prohibitedKeywordDTO) {
        if (prohibitedKeywordDTO == null) {
            return null;
        }

        return ProhibitedKeyword.builder()
                .keyword(prohibitedKeywordDTO.getKeyword())
                .active(prohibitedKeywordDTO.isActive())
                .createdAt(prohibitedKeywordDTO.getCreatedAt())
                .updatedAt(prohibitedKeywordDTO.getUpdatedAt())
                .build();
    }
}
