package com.market.analysis.application.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for StrategyObjective.
 * Carries risk management configuration between the presentation layer and views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyObjectiveDTO {

    private String targetType;
    private BigDecimal targetValue;
    private String stopLossType;
    private BigDecimal stopLossValue;
    private BigDecimal capitalToRisk;
    private String description;
}
