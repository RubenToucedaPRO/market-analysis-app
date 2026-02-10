package com.market.analysis.application.usecase;

import java.util.List;

import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.infrastructure.external.finnhub.FinnhubAdapter;

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
    private final FinnhubAdapter finnhubAdapter;

    @Override
    public void getTickerData(String tickers) {
        List<String> tickerList = parseTickers(tickers);
        checkCompanyProfile(tickerList);

        for (String ticker : tickerList) {
            Stock stock = finnhubAdapter.getQuote(ticker);
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
                .toList();
    }

    /**
     * Checks if company profiles exist and are up-to-date for the given list of
     * tickers.
     * 
     * @param tickerList
     */
    private void checkCompanyProfile(List<String> tickerList) {
        List<String> companiesToUpdate = new java.util.ArrayList<>();

        for (String ticker : tickerList) {
            CompanyProfile companyProfile = companyProfileRepository.findByTicker(ticker);
            if (companyProfile == null) {
                companiesToUpdate.add(ticker);
            } else if (companyProfile.getLastUpdated() == null || companyProfile.isOutdated()) {
                companyProfileRepository.deleteByTicker(ticker);
                companiesToUpdate.add(ticker);
            }
        }

        companiesToUpdate.stream()
                .forEach(ticker -> {
                    CompanyProfile companyProfile = finnhubAdapter.getCompanyProfile(ticker);
                    if (companyProfile != null) {
                        if (checkProfile(ticker, companyProfile.getName())) {
                            prohibitedTickerRepository.save(new ProhibitedTicker(ticker));
                            log.info("Ticker {} marked as prohibited based on company name '{}'", ticker,
                                    companyProfile.getName());
                            tickerList.remove(ticker);
                        } else {
                            companyProfileRepository.save(companyProfile);
                        }
                    }
                });
    }

    /**
     * Checks both prohibited status and missing profile in a single API call.
     * This is more efficient than calling isProhibitedTicker and isMissingProfile
     * separately.
     * 
     * @param ticker the stock ticker symbol
     * @return ProfileCheckResult containing both prohibited and missing profile
     *         status
     */
    public boolean checkProfile(String ticker, String companyName) {
        log.debug("Checking profile for ticker {}", ticker);

        String companyNameUpper = companyName.toUpperCase();
        boolean isProhibited = PROHIBITED_KEYWORDS.stream()
                .anyMatch(companyNameUpper::contains);

        log.debug("Profile check for {}: name='{}', isProhibited={}",
                ticker, companyName, isProhibited);
        return isProhibited;
    }

}
