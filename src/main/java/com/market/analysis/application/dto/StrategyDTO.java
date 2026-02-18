package com.market.analysis.application.dto;

import java.math.BigDecimal;
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
    
    // Objective fields for Risk:Reward calculation
    private String targetType;        // "SMA", "PERCENTAGE", or "FIXED_PRICE"
    private BigDecimal targetValue;
    private String stopLossType;      // "SMA", "PERCENTAGE", or "FIXED_PRICE"
    private BigDecimal stopLossValue;
    private BigDecimal capitalToRisk;
    private String objectiveDescription;
}
