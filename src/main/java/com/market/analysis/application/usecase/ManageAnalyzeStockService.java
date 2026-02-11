package com.market.analysis.application.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.ValidationRule;
import com.market.analysis.domain.model.ValidationRuleFactory;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ManageAnalyzeStockService implements ManageAnalyzeTickerUseCase {

    private final StockDataRepository stockDataRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final ProhibitedTickerRepository prohibitedTickerRepository;
    private final StockProviderPort stockProviderPort;

    @Override
    public void getStockData(String tickers, String ruleId) {
        List<String> tickerList = parseTickers(tickers);
        List<String> validTickers = validateAndUpdateCompanyProfiles(tickerList);

        for (String ticker : validTickers) {
            Stock stock = stockProviderPort.getQuote(ticker);
            if (stock != null) {
                // Apply validation rule if provided
                if (ruleId != null && !ruleId.isEmpty()) {
                    applyValidationRule(stock, ruleId);
                }
                stockDataRepository.saveStockData(stock);
            }
        }
    }

    @Override
    public List<Stock> findAllStocks() {
        return stockDataRepository.findAllStocks();
    }

    @Override
    public Stock findStockDataByTicker(String ticker) {
        return stockDataRepository.findByTicker(ticker)
                .orElseThrow(() -> new StockDataNotFoundException("Ticker data not found for: " + ticker));
    }

    @Override
    public void updateStockData(String ticker) {
        Stock stock = stockProviderPort.getQuote(ticker);
        if (stock != null) {
            stockDataRepository.updateStockData(stock);
        } else {
            log.warn("No stock data found for ticker {}, skipping update", ticker);
        }
    }

    @Override
    public void deleteStockDataByTicker(String ticker) {
        stockDataRepository.deleteByTicker(ticker);
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
        CompanyProfile companyProfile = companyProfileRepository.findByTicker(ticker).orElse(null);
        return companyProfile == null || (companyProfile.getLastUpdated() == null || companyProfile.isOutdated());
    }

    private void updateCompanyProfile(List<String> validTickers, String ticker) {
        if (prohibitedTickerRepository.existsByTicker(ticker)) {
            log.info("Ticker {} is already marked as prohibited, skipping profile check", ticker);
            return;
        }
        CompanyProfile companyProfile = stockProviderPort.getCompanyProfile(ticker);
        if (companyProfile == null) {
            log.warn("No company profile found for ticker {}, skipping", ticker);
            return;
        }
        if (companyProfile.isProhibited()) {
            ProhibitedTicker newProhibitedTicker = ProhibitedTicker.createProhibited(ticker,
                    companyProfile.getProhibitionReason());
            prohibitedTickerRepository.save(newProhibitedTicker);
            log.info("Ticker {} marked as prohibited based on company profile by '{}'", ticker,
                    companyProfile.getProhibitionReason());
            return;
        }
        validTickers.add(ticker);
        companyProfileRepository.save(companyProfile);
        log.info("Company profile for ticker {} saved/updated successfully", ticker);
    }

    /**
     * Applies a validation rule to a stock and stores the result.
     * 
     * @param stock  the stock to validate
     * @param ruleId the ID of the rule to apply
     */
    private void applyValidationRule(Stock stock, String ruleId) {
        Optional<ValidationRule> ruleOpt = ValidationRuleFactory.getRuleById(ruleId);
        
        if (ruleOpt.isEmpty()) {
            log.warn("Validation rule with ID {} not found, skipping validation", ruleId);
            return;
        }

        ValidationRule rule = ruleOpt.get();
        boolean result = rule.evaluate(stock);
        
        stock.setAppliedRuleId(ruleId);
        stock.setRuleValidationResult(result);
        
        log.info("Applied rule '{}' to ticker {}: result = {}", 
                 rule.getRuleName(), stock.getTicker(), result);
    }

}
