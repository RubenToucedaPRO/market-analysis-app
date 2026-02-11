package com.market.analysis.infrastructure.external.finnhub;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.infrastructure.external.finnhub.dto.CompanyData;
import com.market.analysis.infrastructure.external.finnhub.dto.QuoteData;

@Component
public class FinnhubMapper {

    public Stock toDomain(QuoteData quote) {
        if (quote == null || !quote.isValid()) {
            return null;
        }
        return Stock.builder()
                .ticker(quote.getSymbol())
                .currentPrice(quote.getC())
                .highOfDay(quote.getH())
                .lowOfDay(quote.getL())
                .openPrice(quote.getO())
                .previousClose(quote.getPc())
                .lastUpdated(LocalDateTime.ofEpochSecond(quote.getT(), 0, java.time.ZoneOffset.UTC))
                .build();
    }

    public CompanyProfile toDomain(CompanyData profile) {
        if (profile == null || !profile.isValid()) {
            return null;
        }
        return CompanyProfile.builder()
                .ticker(profile.getTicker())
                .name(profile.getName())
                .country(profile.getCountry())
                .exchange(profile.getExchange())
                .industry(profile.getFinnhubIndustry())
                .ipo(profile.getIpo())
                .logo(profile.getLogo())
                .marketCapitalization(profile.getMarketCapitalization())
                .shareOutstanding(profile.getShareOutstanding())
                .website(profile.getWeburl())
                .lastUpdated(profile.getLastUpdated())
                .build();
    }

}
