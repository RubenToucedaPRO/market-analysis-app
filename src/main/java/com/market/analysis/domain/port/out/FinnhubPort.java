package com.market.analysis.domain.port.out;

import com.market.analysis.domain.model.CompanyProfileData;
import com.market.analysis.domain.model.TickerData;

public interface FinnhubPort {

    TickerData getQuote(String ticker);

    CompanyProfileData getCompanyProfile(String ticker);

    boolean hasUpComingEarnings(String ticker);
}
