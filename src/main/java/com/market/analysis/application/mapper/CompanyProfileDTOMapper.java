package com.market.analysis.application.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.application.dto.CompanyProfileDto;
import com.market.analysis.domain.model.CompanyProfile;

@Component
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
