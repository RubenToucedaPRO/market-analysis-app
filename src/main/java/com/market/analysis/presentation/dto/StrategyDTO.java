package com.market.analysis.presentation.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Strategy.
 * Used to transfer strategy data between the presentation layer and views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyDTO {

    private Long id;
    private String name;
    private String description;
    private List<RuleDTO> rules;
}
