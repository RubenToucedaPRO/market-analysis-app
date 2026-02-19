package com.market.analysis.application.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.application.dto.StrategyObjectiveDTO;
import com.market.analysis.domain.model.ObjectiveType;
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

        return StrategyDTO.builder()
                .id(strategy.getId())
                .name(strategy.getName())
                .description(strategy.getDescription())
                .rules(ruleDTOMapper.toDTOList(strategy.getRules()))
                .objective(toObjectiveDTO(strategy.getObjective()))
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
                .objective(toObjectiveDomain(dto.getObjective()))
                .build();
    }

    private StrategyObjectiveDTO toObjectiveDTO(StrategyObjective objective) {
        if (objective == null) {
            return null;
        }
        return StrategyObjectiveDTO.builder()
                .targetType(objective.getTargetType() != null ? objective.getTargetType().name() : null)
                .targetValue(objective.getTargetValue())
                .stopLossType(objective.getStopLossType() != null ? objective.getStopLossType().name() : null)
                .stopLossValue(objective.getStopLossValue())
                .capitalToRisk(objective.getCapitalToRisk())
                .description(objective.getDescription())
                .build();
    }

    private StrategyObjective toObjectiveDomain(StrategyObjectiveDTO dto) {
        if (dto == null) {
            return null;
        }
        return StrategyObjective.builder()
                .targetType(dto.getTargetType() != null ? ObjectiveType.valueOf(dto.getTargetType()) : null)
                .targetValue(dto.getTargetValue())
                .stopLossType(dto.getStopLossType() != null ? ObjectiveType.valueOf(dto.getStopLossType()) : null)
                .stopLossValue(dto.getStopLossValue())
                .capitalToRisk(dto.getCapitalToRisk())
                .description(dto.getDescription())
                .build();
    }
}
