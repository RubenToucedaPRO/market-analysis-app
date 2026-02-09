package com.market.analysis.domain.port.out;

import com.market.analysis.domain.model.CompanyProfileData;

public interface CompanyDataRepository {

    void saveCompanyProfileData(CompanyProfileData companyProfileData);

    CompanyProfileData findCompanyProfileDataByTicker(String ticker);

    void updateCompanyProfileData(CompanyProfileData companyProfileData);

    void deleteCompanyProfileDataByTicker(String ticker);
}
