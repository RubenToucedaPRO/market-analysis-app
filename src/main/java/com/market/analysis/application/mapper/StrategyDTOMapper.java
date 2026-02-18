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
            builder.targetType(objective.getTargetType().name())
                    .targetValue(objective.getTargetValue())
                    .stopLossType(objective.getStopLossType().name())
                    .stopLossValue(objective.getStopLossValue())
                    .capitalToRisk(objective.getCapitalToRisk())
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
        if (dto.getTargetType() != null && dto.getStopLossType() != null) {
            objective = StrategyObjective.builder()
                    .targetType(StrategyObjective.ObjectiveType.valueOf(dto.getTargetType()))
                    .targetValue(dto.getTargetValue())
                    .stopLossType(StrategyObjective.ObjectiveType.valueOf(dto.getStopLossType()))
                    .stopLossValue(dto.getStopLossValue())
                    .capitalToRisk(dto.getCapitalToRisk())
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
