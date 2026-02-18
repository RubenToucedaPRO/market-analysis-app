package com.market.analysis.application.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyObjective;

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

        StrategyDTO.StrategyDTOBuilder builder = StrategyDTO.builder()
                .id(strategy.getId())
                .name(strategy.getName())
                .description(strategy.getDescription())
                .rules(ruleDTOMapper.toDTOList(strategy.getRules()));

        // Map objective if present
        if (strategy.hasObjective()) {
            StrategyObjective objective = strategy.getObjective();
            builder.targetPrice(objective.getTargetPrice())
                    .stopLossPrice(objective.getStopLossPrice())
                    .positionType(objective.getPositionType().name())
                    .objectiveDescription(objective.getDescription());
        }

        return builder.build();
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

        // Build objective if present in DTO
        StrategyObjective objective = null;
        if (dto.getTargetPrice() != null && dto.getStopLossPrice() != null && dto.getPositionType() != null) {
            objective = StrategyObjective.builder()
                    .targetPrice(dto.getTargetPrice())
                    .stopLossPrice(dto.getStopLossPrice())
                    .positionType(StrategyObjective.PositionType.valueOf(dto.getPositionType()))
                    .description(dto.getObjectiveDescription())
                    .build();
        }

        return Strategy.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .rules(ruleDTOMapper.toDomainList(dto.getRules()))
                .objective(objective)
                .build();
    }
}
