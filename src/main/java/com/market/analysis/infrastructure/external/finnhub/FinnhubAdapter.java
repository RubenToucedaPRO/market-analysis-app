package com.market.analysis.infrastructure.external.finnhub;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.infrastructure.exception.FinnhubException;
import com.market.analysis.infrastructure.external.finnhub.dto.CompanyData;
import com.market.analysis.infrastructure.external.finnhub.dto.QuoteData;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class FinnhubAdapter implements StockProviderPort {

    @Qualifier("finnhubRestClient")
    private final RestClient restClient;
    private final FinnhubMapper finnhubMapper;

    public static final String SYMBOL = "symbol";
    public static final String TOKEN = "token";

    @Value("${finnhub.api.token:}")
    private String apiToken;

    @Override
    @RateLimiter(name = "finnhubClient")
    public Stock getQuote(String ticker) {
        log.debug("Fetching quote for ticker: {}", ticker);

        try {
            // Sintaxis fluida, limpia y SIN .block()
            QuoteData quote = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/quote")
                            .queryParam(SYMBOL, ticker)
                            .queryParam(TOKEN, apiToken)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.value() == 429, (request, response) -> {
                        throw new FinnhubException("Limit exceeded when fetching quote for " + ticker);
                    })
                    .body(QuoteData.class);

            quote.setSymbol(ticker);
            if (quote == null || !quote.isValid()) {
                throw new FinnhubException("No valid data found for: " + ticker);
            }
            log.debug("Quote fetched for {}: price={}", ticker, quote.getC());
            return finnhubMapper.toDomain(quote);

        } catch (FinnhubException e) {
            throw e;

        } catch (HttpClientErrorException e) {
            throw new FinnhubException("API error for " + ticker, e);

        } catch (Exception e) {
            throw new FinnhubException("Unexpected error fetching quote " + ticker + ": " + e.getMessage(), e);
        }
    }

    @Override
    @RateLimiter(name = "finnhubClient")
    public CompanyProfile getCompanyProfile(String ticker) {

        try {
            CompanyData profile = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/stock/profile2")
                            .queryParam(SYMBOL, ticker)
                            .queryParam(TOKEN, apiToken)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        if (response.getStatusCode().value() == 429) {
                            throw new FinnhubException("Limit exceeded when fetching quote for " + ticker);
                        }
                    })
                    .body(CompanyData.class);

            if (profile != null && profile.isValid()) {
                log.debug("Profile fetched for {}: name={}", ticker, profile.getName());
                profile.setTicker(ticker);
                profile.setLastUpdated(java.time.Instant.now());
                return finnhubMapper.toDomain(profile);
            }
            log.debug("No valid profile found for {}", ticker);
            return null;

        } catch (FinnhubException e) {
            log.warn("Rate limit hit or specific error for {}: {}", ticker, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.warn("Error fetching profile for ticker {}: {}", ticker, e.getClass().getSimpleName());
            return null;
        }
    }

}
