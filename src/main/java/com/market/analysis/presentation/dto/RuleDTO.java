package com.market.analysis.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Rule.
 * Used to transfer rule data between the presentation layer and views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleDTO {

    private Long id;
    private String name;
    private String subjectCode;
    private Double subjectParam;
    private String operator;
    private String targetCode;
    private Double targetParam;
    private String description;
}
