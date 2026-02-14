package com.market.analysis.application.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.domain.model.Strategy;

import lombok.RequiredArgsConstructor;

/**
 * Mapper to convert between Strategy domain model and StrategyDTO.
 * Part of the presentation layer, handling translation between domain and DTOs.
 */
@Component
@RequiredArgsConstructor
public class StrategyDTOMapper {

    private final RuleDTOMapper ruleDTOMapper;

    /**
     * Converts a Strategy domain model to a StrategyDTO.
     * 
     * @param strategy the domain model
     * @return the DTO
     */
    public StrategyDTO toDTO(Strategy strategy) {
        if (strategy == null) {
            return null;
        }

        return StrategyDTO.builder()
                .id(strategy.getId())
                .name(strategy.getName())
                .description(strategy.getDescription())
                .rules(strategy.getRules() != null 
                    ? ruleDTOMapper.toDTOList(strategy.getRules())
                    : java.util.Collections.emptyList())
                .build();
    }

    /**
     * Converts a StrategyDTO to a Strategy domain model.
     * 
     * @param dto the DTO
     * @return the domain model
     */
    public Strategy toDomain(StrategyDTO dto) {
        if (dto == null) {
            return null;
        }

        return Strategy.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .rules(dto.getRules() != null
                    ? ruleDTOMapper.toDomainList(dto.getRules())
                    : java.util.Collections.emptyList())
                .build();
    }
}
