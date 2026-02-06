package com.market.analysis.presentation.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.Rule;
import com.market.analysis.presentation.dto.RuleDTO;

/**
 * Mapper to convert between Rule domain model and RuleDTO.
 * Part of the presentation layer, handling translation between domain and DTOs.
 */
@Component
public class RuleDTOMapper {

    /**
     * Converts a Rule domain model to a RuleDTO.
     * 
     * @param rule the domain model
     * @return the DTO
     */
    public RuleDTO toDTO(Rule rule) {
        if (rule == null) {
            return null;
        }

        return RuleDTO.builder()
                .id(rule.getId())
                .name(rule.getName())
                .subjectCode(rule.getSubjectCode())
                .subjectParam(rule.getSubjectParam())
                .operator(rule.getOperator())
                .targetCode(rule.getTargetCode())
                .targetParam(rule.getTargetParam())
                .description(rule.getDescription())
                .build();
    }

    /**
     * Converts a RuleDTO to a Rule domain model.
     * 
     * @param dto the DTO
     * @return the domain model
     */
    public Rule toDomain(RuleDTO dto) {
        if (dto == null) {
            return null;
        }

        return Rule.builder()
                .id(dto.getId())
                .name(dto.getName())
                .subjectCode(dto.getSubjectCode())
                .subjectParam(dto.getSubjectParam())
                .operator(dto.getOperator())
                .targetCode(dto.getTargetCode())
                .targetParam(dto.getTargetParam())
                .description(dto.getDescription())
                .build();
    }

    /**
     * Converts a list of Rule domain models to a list of RuleDTOs.
     * 
     * @param rules the domain models
     * @return the DTOs
     */
    public List<RuleDTO> toDTOList(List<Rule> rules) {
        if (rules == null) {
            return List.of();
        }

        return rules.stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Converts a list of RuleDTOs to a list of Rule domain models.
     * 
     * @param dtos the DTOs
     * @return the domain models
     */
    public List<Rule> toDomainList(List<RuleDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }

        return dtos.stream()
                .map(this::toDomain)
                .toList();
    }
}
