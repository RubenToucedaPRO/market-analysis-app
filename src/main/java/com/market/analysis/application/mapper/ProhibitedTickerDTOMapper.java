package com.market.analysis.application.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.application.dto.ProhibitedTickerDTO;
import com.market.analysis.domain.model.ProhibitedTicker;

@Component
public class ProhibitedTickerDTOMapper {

    /**
     * Mapper class to convert between ProhibitedTicker domain model and
     * ProhibitedTickerDTO.
     * This class is responsible for translating the domain model to a format
     * suitable for the presentation layer and vice versa.
     */
    public ProhibitedTickerDTO toDTO(ProhibitedTicker prohibitedTicker) {
        if (prohibitedTicker == null) {
            return null;
        }

        return ProhibitedTickerDTO.builder()
                .ticker(prohibitedTicker.getTicker())
                .reason(prohibitedTicker.getReason())
                .createdAt(prohibitedTicker.getCreatedAt())
                .build();
    }

    public ProhibitedTicker toDomain(ProhibitedTickerDTO prohibitedTickerDTO) {
        if (prohibitedTickerDTO == null) {
            return null;
        }
        return ProhibitedTicker.builder()
                .ticker(prohibitedTickerDTO.getTicker())
                .reason(prohibitedTickerDTO.getReason())
                .createdAt(prohibitedTickerDTO.getCreatedAt())
                .build();
    }
}
