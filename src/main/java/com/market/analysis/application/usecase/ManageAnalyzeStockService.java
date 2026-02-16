package com.market.analysis.application.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.market.analysis.application.dto.StockDataDTO;
import com.market.analysis.application.mapper.StockDataDTOMapper;
import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.HistoricalData;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.model.TechnicalIndicators;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.out.ApiCallRateRepository;
import com.market.analysis.domain.port.out.ApiIAPort;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.domain.port.out.HistoricalProviderPort;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.service.EvaluateStrategyService;
import com.market.analysis.domain.service.StockHistoricalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ManageAnalyzeStockService implements ManageAnalyzeTickerUseCase {

    private final StockDataRepository stockDataRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final ProhibitedTickerRepository prohibitedTickerRepository;
    private final StrategyEvaluationRepository strategyEvaluationRepository;
    private final ApiCallRateRepository apiCallRateRepository;
    private final StockProviderPort stockProviderPort;
    private final HistoricalProviderPort historicalProviderPort;
    private final ApiIAPort apiIAPort;
    private final StrategyRepository strategyRepository;
    private final StockDataDTOMapper stockMapper;

    private final StockHistoricalService stockHistoricalService;
    private final EvaluateStrategyService evaluateStrategyServ;

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
                StrategyEvaluation evaluationResult = evaluateStrategyServ.evaluateStrategy(strategy, savedStock);

                // Save the evaluation result
                strategyEvaluationRepository.save(evaluationResult, savedStock);

                log.info("Ticker {} added with strategy '{}': {}",
                        ticker,
                        strategy.getName(),
                        evaluationResult.isCompliant() ? "PASSED" : "FAILED");
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
            existingStockData.setCurrentPrice(stock.getCurrentPrice());
            existingStockData.setOpenPrice(stock.getOpenPrice());
            existingStockData.setHighOfDay(stock.getHighOfDay());
            existingStockData.setLowOfDay(stock.getLowOfDay());
            existingStockData.setPreviousClose(stock.getPreviousClose());
            stockDataRepository.save(existingStockData);
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
        if (stock == null) {
            log.warn("No stock quote found for ticker: {}", ticker);
            return null;
        }
        ZoneId zone = ZoneId.of("America/New_York");
        Instant startOfToday = LocalDate.now(zone)
                .atStartOfDay(zone)
                .toInstant();

        Instant startOfTomorrow = LocalDate.now(zone)
                .plusDays(1)
                .atStartOfDay(zone)
                .toInstant();
        Stock existingStock = stockDataRepository.findByTickerAndLastUpdateBetween(ticker, startOfToday,
                startOfTomorrow);
        if (existingStock != null) {
            log.info("Using historical existing stock data for ticker: {}", ticker);
            stock.setSma20(existingStock.getSma20());
            stock.setSma50(existingStock.getSma50());
            stock.setSma200(existingStock.getSma200());
            stock.setVolume(existingStock.getVolume());
            stock.setAverageVolume(existingStock.getAverageVolume());
            stock.setLastUpdated(existingStock.getLastUpdated());
        } else {
            log.info("Fetching new stock data for ticker: {}", ticker);
            HistoricalData historicalData = historicalProviderPort.fetchHistoricalData(ticker);
            if (historicalData != null) {
                apiCallRateRepository.save(ticker, historicalData.getLastUpdate());
                log.info("Historical data for ticker {} fetched and saved successfully", ticker);
            }
            TechnicalIndicators technicalIndicators = stockHistoricalService.calculateIndicators(historicalData,
                    20);
            stock.setSma20(technicalIndicators.getSma20());
            stock.setSma50(technicalIndicators.getSma50());
            stock.setSma200(technicalIndicators.getSma200());
            stock.setVolume(technicalIndicators.getCurrentVolume());
            stock.setAverageVolume(technicalIndicators.getAverageVolume());
            stock.setLastUpdated(technicalIndicators.getLastUpdated());
        }

        return stock;
    }

    @Override
    public void getValorationIA(Long id) {
        Stock stock = stockDataRepository.findById(id)
                .orElseThrow(() -> new StockDataNotFoundException("Ticker data not found for: " + id));
        String ticker = stock.getTicker();
        String datosAccion = String.format(
                "Ticker: %s, Price: %.2f, SMA20: %.2f, SMA50: %.2f, SMA200: %.2f, Volume: %d, Average Volume: %d, Strategy: %s, Compliance Rate: %.2f, Summary: %s",
                ticker, stock.getCurrentPrice(), stock.getSma20(), stock.getSma50(), stock.getSma200(),
                stock.getVolume(),
                stock.getAverageVolume(), stock.getStrategyEvaluation().getStrategyName(),
                stock.getStrategyEvaluation().getComplianceRate(), stock.getStrategyEvaluation().getSummary());

        String prompt = "You are an expert financial analyst."
                + "This is a placeholder valuation based on the stock data: " + datosAccion
                + ". I want you to analyze this stock data and provide a valuation of the stock. "
                + " Please consider the current price, technical indicators (SMA20, SMA50, SMA200), volume, average volume, and the strategy evaluation results (compliance rate and summary)."
                + " Based on this information, provide a concise valuation of the stock's potential performance in the market."
                + " You answer in a single sentence and be as specific as possible, providing insights on the stock's strengths, weaknesses, and overall outlook."
                + " Remember answer in spanish.";

        String valoration = apiIAPort.getValoration(prompt);
        log.info("Valuation for ticker {}: {}", ticker, valoration);
        stock.setValorationIA(valoration);
        stockDataRepository.save(stock);
    }
}
