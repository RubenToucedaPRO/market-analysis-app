package com.market.analysis.presentation.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.presentation.dto.ProhibitedTickerDTO;

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
                .build();
    }

    public ProhibitedTicker toDomain(ProhibitedTickerDTO prohibitedTickerDTO) {
        if (prohibitedTickerDTO == null) {
            return null;
        }

        return new ProhibitedTicker(prohibitedTickerDTO.getTicker());
    }
}
