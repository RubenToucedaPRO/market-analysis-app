package com.market.analysis.domain.port.out;

import java.util.Optional;

import com.market.analysis.domain.model.CompanyProfile;

public interface CompanyProfileRepository {

    void save(CompanyProfile companyProfile);

    Optional<CompanyProfile> findByTicker(String ticker);

    void update(CompanyProfile companyProfile);

    void deleteByTicker(String ticker);
}
