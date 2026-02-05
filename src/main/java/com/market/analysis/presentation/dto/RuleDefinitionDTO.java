package com.market.analysis.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for RuleDefinition.
 * Used to transfer rule definition data between the presentation layer and views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleDefinitionDTO {
    
    private Long id;
    private String code;
    private String name;
    private boolean requiresParam;
    private String description;
}
