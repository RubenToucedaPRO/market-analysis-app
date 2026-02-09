package com.market.analysis.infrastructure.external.finnhub;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.market.analysis.domain.model.CompanyProfileData;
import com.market.analysis.domain.model.TickerData;
import com.market.analysis.domain.port.out.FinnhubPort;
import com.market.analysis.infrastructure.exception.FinnhubException;
import com.market.analysis.infrastructure.external.finnhub.dto.QuoteData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class FinnhubAdapter implements FinnhubPort {

    @Qualifier("finnhubRestClient")
    private final RestClient restClient;
    private final FinnhubMapper finnhubMapper;

    public static final String SYMBOL = "symbol";
    public static final String TOKEN = "token";

    @Value("${finnhub.api.token:}")
    private String apiToken;

    @Override
    public TickerData getQuote(String ticker) {
        log.debug("Fetching quote for ticker: {}", ticker);

        try {
            // Sintaxis fluida, limpia y SIN .block()
            QuoteData quote = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/quote")
                            .queryParam(SYMBOL, ticker.toUpperCase())
                            .queryParam(TOKEN, apiToken)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.value() == 429, (request, response) -> {
                        throw new FinnhubException("Limit exceeded when fetching quote for " + ticker);
                    })
                    .body(QuoteData.class);

            if (quote == null || !quote.isValid()) {
                throw new FinnhubException("No valid data found for: " + ticker);
            }
            quote.setSymbol(ticker.toUpperCase());
            log.debug("Quote fetched for {}: price={}", ticker, quote.getC());
            return finnhubMapper.toDomain(quote);

        } catch (FinnhubException e) {
            throw e;

        } catch (HttpClientErrorException e) {
            log.error("API error fetching quote for {}: Status {}", ticker, e.getStatusCode());
            throw new FinnhubException("API error for " + ticker, e);

        } catch (Exception e) {
            log.error("Error fetching quote for ticker {}: {}", ticker, e.getClass().getSimpleName());
            throw new FinnhubException("Unexpected error fetching quote", e);
        }
    }

    @Override
    public CompanyProfileData getCompanyProfile(String ticker) {
        return null;
    }

    @Override
    public boolean hasUpComingEarnings(String ticker) {
        return false;
    }

}
