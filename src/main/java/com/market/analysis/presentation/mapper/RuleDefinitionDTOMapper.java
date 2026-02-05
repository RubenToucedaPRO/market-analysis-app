package com.market.analysis.presentation.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.presentation.dto.RuleDefinitionDTO;

/**
 * Mapper to convert between RuleDefinition domain model and RuleDefinitionDTO.
 * Part of the presentation layer, handling translation between domain and DTOs.
 */
@Component
public class RuleDefinitionDTOMapper {

    /**
     * Converts a RuleDefinition domain model to a RuleDefinitionDTO.
     * 
     * @param ruleDefinition the domain model
     * @return the DTO
     */
    public RuleDefinitionDTO toDTO(RuleDefinition ruleDefinition) {
        if (ruleDefinition == null) {
            return null;
        }
        
        return RuleDefinitionDTO.builder()
                .id(ruleDefinition.getId())
                .code(ruleDefinition.getCode())
                .name(ruleDefinition.getName())
                .requiresParam(ruleDefinition.isRequiresParam())
                .description(ruleDefinition.getDescription())
                .build();
    }

    /**
     * Converts a RuleDefinitionDTO to a RuleDefinition domain model.
     * 
     * @param dto the DTO
     * @return the domain model
     */
    public RuleDefinition toDomain(RuleDefinitionDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return RuleDefinition.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .name(dto.getName())
                .requiresParam(dto.isRequiresParam())
                .description(dto.getDescription())
                .build();
    }
}
