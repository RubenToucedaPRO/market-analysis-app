package com.market.analysis.presentation.mapper;

import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.presentation.dto.CompanyProfileDto;

public class CompanyProfileDTOMapper {

    public CompanyProfileDto toDTO(CompanyProfile companyProfile) {
        if (companyProfile == null) {
            return null;
        }

        return CompanyProfileDto.builder()
                .ticker(companyProfile.getTicker())
                .name(companyProfile.getName())
                .industry(companyProfile.getIndustry())
                .website(companyProfile.getWebsite())
                .logo(companyProfile.getLogo())
                .build();
    }
}
