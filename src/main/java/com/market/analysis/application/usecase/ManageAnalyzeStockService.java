package com.market.analysis.application.usecase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.market.analysis.application.dto.StockDataDTO;
import com.market.analysis.application.mapper.StockDataDTOMapper;
import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.domain.model.AnalysisResult;
import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.HistoricalData;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.TechnicalIndicators;
import com.market.analysis.domain.port.in.EvaluateStrategyUseCase;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.out.ApiCallRateRepository;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.domain.port.out.HistoricalProviderPort;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.service.StockHistoricalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ManageAnalyzeStockService implements ManageAnalyzeTickerUseCase {

    private final StockDataRepository stockDataRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final ProhibitedTickerRepository prohibitedTickerRepository;
    private final ApiCallRateRepository apiCallRateRepository;
    private final StockProviderPort stockProviderPort;
    private final HistoricalProviderPort historicalProviderPort;
    private final StrategyRepository strategyRepository;
    private final EvaluateStrategyUseCase evaluateStrategyUseCase;
    private final StockDataDTOMapper stockMapper;
    private final StockHistoricalService stockHistoricalService;

    @Override
    public void getStockData(String tickers, Long strategyId) {
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy ID is required");
        }

        // Load the strategy
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found with id: " + strategyId));

        List<String> tickerList = parseTickers(tickers);
        List<String> validTickers = validateAndUpdateCompanyProfiles(tickerList);

        for (String ticker : validTickers) {
            Stock stock = getdataFromProvider(ticker);

            if (stock != null) {
                // Set the strategy ID
                stock.setStrategyId(strategyId);

                // Save the stock data
                Stock savedStock = stockDataRepository.save(stock);

                // Evaluate the strategy against the stock data
                AnalysisResult evaluationResult = evaluateStrategyUseCase.evaluateStrategy(strategy, savedStock);
                // Evaluation result is persisted by EvaluateStrategyService

                log.info("Ticker {} added with strategy '{}': {}",
                        ticker,
                        strategy.getName(),
                        evaluationResult.isOverallPassed() ? "PASSED" : "FAILED");
            }
        }
    }

    @Override
    public List<StockDataDTO> findAllStocks() {
        return stockDataRepository.findAllStocks().stream()
                .map(stockMapper::toDTO)
                .toList();
    }

    @Override
    public StockDataDTO findStockDataById(Long id) {
        return stockDataRepository.findById(id).map(stockMapper::toDTO)
                .orElseThrow(() -> new StockDataNotFoundException("Ticker data not found for: " + id));
    }

    @Override
    public void updateStockData(Long id) {
        Stock existingStockData = stockDataRepository.findById(id)
                .orElseThrow(() -> new StockDataNotFoundException("Ticker data not found for: " + id));
        String ticker = existingStockData.getTicker();
        Stock stock = stockProviderPort.getQuote(ticker);
        if (stock != null) {
            stock.setId(id);
            stockDataRepository.save(stock);
        } else {
            log.warn("No stock data found for ticker {}, skipping update", ticker);
        }
    }

    @Override
    public void deleteById(Long id) {
        stockDataRepository.deleteById(id);
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

    private Stock getdataFromProvider(String ticker) {
        Stock stock = stockProviderPort.getQuote(ticker);
        Stock existingStock = stockDataRepository.findByTickerAndDate(ticker, LocalDate.now());
        if (existingStock != null) {
            log.info("Using historical existing stock data for ticker: {}", ticker);
            stock.setSma20(existingStock.getSma20());
            stock.setSma50(existingStock.getSma50());
            stock.setSma200(existingStock.getSma200());
            stock.setVolume(existingStock.getVolume());
            stock.setAverageVolume(existingStock.getAverageVolume());
        } else {
            log.info("Fetching new stock data for ticker: {}", ticker);
            HistoricalData historicalData = historicalProviderPort.fetchHistoricalData(ticker);
            if (historicalData != null) {
                apiCallRateRepository.save(ticker, LocalDate.now());
                log.info("Historical data for ticker {} fetched and saved successfully", ticker);
            }
            TechnicalIndicators technicalIndicators = stockHistoricalService.calculateIndicators(historicalData,
                    20);
            stock.setSma20(technicalIndicators.getSma20());
            stock.setSma50(technicalIndicators.getSma50());
            stock.setSma200(technicalIndicators.getSma200());
            stock.setVolume(technicalIndicators.getCurrentVolume());
            stock.setAverageVolume(technicalIndicators.getAverageVolume());
        }

        return stock;
    }
}
