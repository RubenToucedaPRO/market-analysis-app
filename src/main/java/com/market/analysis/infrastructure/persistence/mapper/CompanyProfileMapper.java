package com.market.analysis.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.infrastructure.persistence.entity.CompanyProfileEntity;

@Component
public class CompanyProfileMapper {

    public CompanyProfileEntity toEntity(CompanyProfile profile) {
        if (profile == null) {
            return null;
        }
        return CompanyProfileEntity.builder()
                .name(profile.getName())
                .country(profile.getCountry())
                .ticker(profile.getTicker())
                .exchange(profile.getExchange())
                .industry(profile.getIndustry())
                .ipo(profile.getIpo())
                .logo(profile.getLogo())
                .marketCapitalization(profile.getMarketCapitalization())
                .shareOutstanding(profile.getShareOutstanding())
                .website(profile.getWebsite())
                .lastUpdated(profile.getLastUpdated())
                .build();
    }

    public CompanyProfile toDomain(CompanyProfileEntity entity) {
        if (entity == null) {
            return null;
        }
        return CompanyProfile.builder()
                .name(entity.getName())
                .ticker(entity.getTicker())
                .exchange(entity.getExchange())
                .industry(entity.getIndustry())
                .ipo(entity.getIpo())
                .logo(entity.getLogo())
                .marketCapitalization(entity.getMarketCapitalization())
                .shareOutstanding(entity.getShareOutstanding())
                .website(entity.getWebsite())
                .lastUpdated(entity.getLastUpdated())
                .build();
    }
}
