package com.market.analysis.application.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.domain.port.out.FinnhubPort;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.domain.port.out.StockDataRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ManageAnalyzeStockService implements ManageAnalyzeTickerUseCase {

    /**
     * Keywords that indicate a ticker should be marked as prohibited.
     * Includes ETFs, funds, SPACs, biotech, and leveraged products.
     */
    private static final List<String> PROHIBITED_KEYWORDS = List.of(
            "ACQUISITION", "MERGER", "ETF", "FUND", "TRUST",
            "BULL", "BEAR", "2X", "3X",
            "THERAPEUTICS", "PHARMA", "BIO", "ONCOLOGY",
            "LP", "PARTNERS", "WARRANTS");

    private final StockDataRepository tickerDataRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final ProhibitedTickerRepository prohibitedTickerRepository;
    private final FinnhubPort finnhubPort;

    @Override
    @Transactional
    public void getTickerData(String tickers) {
        List<String> tickerList = parseTickers(tickers);
        List<String> validTickers = validateAndUpdateCompanyProfiles(tickerList);

        for (String ticker : validTickers) {
            Stock stock = finnhubPort.getQuote(ticker);
            if (stock != null) {
                tickerDataRepository.saveTickerData(stock);
            }
        }
    }

    @Override
    public List<Stock> findAllTickers() {
        return tickerDataRepository.findAllTickers();
    }

    @Override
    public Stock findTickerDataByTicker(String ticker) {
        return tickerDataRepository.findByTicker(ticker)
                .orElseThrow(() -> new StockDataNotFoundException("Ticker data not found for: " + ticker));
    }

    @Override
    public void updateTickerData(Stock tickerData) {
        tickerDataRepository.updateTickerData(tickerData);
    }

    @Override
    public void deleteTickerDataByTicker(String ticker) {
        tickerDataRepository.deleteByTicker(ticker);
    }

    /**
     * Parses a comma-separated string of tickers into a list of uppercase ticker
     * symbols.
     * 
     * @param tickers
     * @return
     */
    private List<String> parseTickers(String tickers) {
        return List.of(tickers.split(",")).stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Checks if company profiles exist and are up-to-date for the given list of
     * tickers.
     * 
     * @param tickerList
     */
    private List<String> validateAndUpdateCompanyProfiles(List<String> tickerList) {
        List<String> validTickers = new ArrayList<>();

        for (String ticker : tickerList) {
            if (isCompanyUpdateRequired(ticker)) {
                updateCompanyProfile(validTickers, ticker);
            } else {
                validTickers.add(ticker);
            }
        }
        return validTickers;
    }

    private void updateCompanyProfile(List<String> validTickers, String ticker) {
        if (prohibitedTickerRepository.existsByTicker(ticker)) {
            log.info("Ticker {} is already marked as prohibited, skipping profile check", ticker);
        } else {
            CompanyProfile companyProfile = finnhubPort.getCompanyProfile(ticker);
            if (companyProfile != null && !isTickerProhibitedByCompanyName(ticker, companyProfile.getName())) {
                validTickers.add(ticker);
                companyProfileRepository.save(companyProfile);
                log.info("Company profile for ticker {} saved/updated successfully", ticker);
            }
        }
    }

    /**
     * Determines if a company profile update is required based on existence and
     * staleness.
     * If the profile is missing or outdated, it will be deleted to ensure fresh
     * data is fetched.
     * 
     * @param ticker the stock ticker symbol
     * @return true if an update is required, false otherwise
     */
    private boolean isCompanyUpdateRequired(String ticker) {
        CompanyProfile companyProfile = companyProfileRepository.findByTicker(ticker);
        return companyProfile == null || (companyProfile.getLastUpdated() == null || companyProfile.isOutdated());
    }

    /**
     * Checks if a ticker should be marked as prohibited based on the presence of
     * certain keywords in the company name. If a prohibited keyword is found, the
     * ticker is saved in the prohibited ticker repository with the reason for
     * prohibition.
     * 
     * @param ticker      the stock ticker symbol
     * @param companyName the name of the company associated with the ticker
     * @return true if the ticker is prohibited, false otherwise
     */
    public boolean isTickerProhibitedByCompanyName(String ticker, String companyName) {
        log.debug("Checking profile for ticker {}", ticker);

        String companyNameUpper = companyName.toUpperCase();
        String reason = PROHIBITED_KEYWORDS.stream()
                .filter(companyNameUpper::contains)
                .findFirst()
                .orElse(null);

        boolean isProhibited = reason != null;
        if (isProhibited) {
            ProhibitedTicker newProhibitedTicker = ProhibitedTicker.builder()
                    .ticker(ticker)
                    .reason(reason)
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
            prohibitedTickerRepository.save(newProhibitedTicker);
            log.info("Ticker {} marked as prohibited based on company name '{}' by '{}'", ticker,
                    companyName, reason);
        }
        return isProhibited;
    }

}
