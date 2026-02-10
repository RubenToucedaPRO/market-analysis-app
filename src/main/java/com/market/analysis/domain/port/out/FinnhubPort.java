package com.market.analysis.domain.port.out;

import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.Stock;

public interface FinnhubPort {

    Stock getQuote(String ticker);

    CompanyProfile getCompanyProfile(String ticker);

    boolean hasUpComingEarnings(String ticker);
}
