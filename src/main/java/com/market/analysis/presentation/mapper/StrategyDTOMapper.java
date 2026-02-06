package com.market.analysis.presentation.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.Strategy;
import com.market.analysis.presentation.dto.StrategyDTO;

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
                .rules(ruleDTOMapper.toDTOList(strategy.getRules()))
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
                .rules(ruleDTOMapper.toDomainList(dto.getRules()))
                .build();
    }
}
