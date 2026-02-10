package com.market.analysis.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileDto {

    private String ticker;

    private String name;

    private String industry;

    private String website;

    private String logo;

}
