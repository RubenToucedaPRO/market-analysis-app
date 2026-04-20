package com.market.analysis.application.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.market.analysis.application.dto.CandleChartDTO;
import com.market.analysis.application.dto.StockDataDTO;
import com.market.analysis.application.mapper.CandleDTOMapper;
import com.market.analysis.application.mapper.StockDataDTOMapper;
import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.domain.model.Candle;
import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.ProhibitedKeyword;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StockOrigin;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.out.ApiIAPort;
import com.market.analysis.domain.port.out.CandleHistoryRepository;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.domain.port.out.ProhibitedKeywordRepository;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.service.ProhibitedKeywordMatcher;
import com.market.analysis.domain.service.PromptBuilder;
import com.market.analysis.domain.service.PromptResponseValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ManageAnalyzeStockService implements ManageAnalyzeTickerUseCase {

    private static final String TICKER_DATA_NOT_FOUND = "Ticker data not found for: ";
    private static final String IA_FALLBACK_VALORATION = "No se pudo generar una valoración interpretativa válida en este momento. Reintenta más tarde.";
    private static final int MAX_PROMPT_CHARS = 4000;

    private final StockDataRepository stockDataRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final ProhibitedKeywordRepository prohibitedKeywordRepository;
    private final ProhibitedTickerRepository prohibitedTickerRepository;
    private final CandleHistoryRepository candleHistoryRepository;
    private final StrategyRepository strategyRepository;
    private final StockProviderPort stockProviderPort;
    private final ApiIAPort apiIAPort;
    private final StockDataDTOMapper stockMapper;
    private final CandleDTOMapper candleDTOMapper;

    private final StockDeterministicAnalysisPipeline stockDeterministicAnalysisPipeline;
    private final PromptBuilder promptBuilder;
    private final ProhibitedKeywordMatcher prohibitedKeywordMatcher;
    private final PromptResponseValidator promptResponseValidator;
    private final AtomicLong aiRequests = new AtomicLong(0);
    private final AtomicLong aiValidResponses = new AtomicLong(0);
    private final AtomicLong aiRetries = new AtomicLong(0);
    private final AtomicLong aiFallbacks = new AtomicLong(0);

    @Override
    public void getStockData(String tickers, Long strategyId) {
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy ID is required");
        }

        // Load the strategy
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found with id: " + strategyId));

        List<String> tickerList = parseTickers(tickers);
        List<ProhibitedKeyword> prohibitedKeywords = prohibitedKeywordRepository.findAll();
        List<String> validTickers = validateAndUpdateCompanyProfiles(tickerList, prohibitedKeywords);

        for (String ticker : validTickers) {
            stockDeterministicAnalysisPipeline.analyzeAndPersist(ticker, strategy, StockOrigin.EXTERNAL_PROVIDER);
        }
    }

    @Override
    public List<StockDataDTO> findAllStocks() {
        return stockDataRepository.findAllStocksVisibleInAnalysis().stream()
                .map(stockMapper::toDTO)
                .toList();
    }

    @Override
    public StockDataDTO findStockDataById(Long id) {
        return stockDataRepository.findById(id).map(stockMapper::toDTO)
                .orElseThrow(() -> new StockDataNotFoundException(TICKER_DATA_NOT_FOUND + id));
    }

    @Override
    public void updateStockData(Long id) {
        Stock existingStockData = stockDataRepository.findById(id)
                .orElseThrow(() -> new StockDataNotFoundException(TICKER_DATA_NOT_FOUND + id));
        String ticker = existingStockData.getTicker();
        Stock stock = stockProviderPort.getQuote(ticker);
        if (stock != null) {
            existingStockData.setCurrentPrice(stock.getCurrentPrice());
            existingStockData.setOpenPrice(stock.getOpenPrice());
            existingStockData.setHighOfDay(stock.getHighOfDay());
            existingStockData.setLowOfDay(stock.getLowOfDay());
            existingStockData.setPreviousClose(stock.getPreviousClose());
            existingStockData.setLastUpdated(Instant.now());
            stockDataRepository.save(existingStockData);
        } else {
            log.warn("No stock data found for ticker {}, skipping update", ticker);
        }
    }

    @Override
    public void deleteById(Long id, String ticker) {
        stockDataRepository.deleteById(id);
        if (!stockDataRepository.existsByTicker(ticker)) {
            log.info("No more stock data exists for ticker {}, deleting associated candles", ticker);
            candleHistoryRepository.deleteCandlesByTicker(ticker);
        } else {
            log.info("Stock data still exists for ticker {}, skipping candle deletion", ticker);
        }
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
    private List<String> validateAndUpdateCompanyProfiles(List<String> tickerList,
            List<ProhibitedKeyword> prohibitedKeywords) {
        List<String> validTickers = new ArrayList<>();

        for (String ticker : tickerList) {
            resolveAndValidateCompanyProfile(validTickers, ticker, prohibitedKeywords);
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
    private boolean isCompanyProfileMissingOrOutdated(CompanyProfile companyProfile) {
        return companyProfile == null || companyProfile.getLastUpdated() == null || companyProfile.isOutdated();
    }

    private void resolveAndValidateCompanyProfile(List<String> validTickers, String ticker,
            List<ProhibitedKeyword> prohibitedKeywords) {
        if (prohibitedTickerRepository.existsByTicker(ticker)) {
            log.info("Ticker {} is already marked as prohibited, skipping profile check", ticker);
            return;
        }
        CompanyProfile companyProfile = companyProfileRepository.findByTicker(ticker).orElse(null);
        boolean companyProfileNeedsRefresh = isCompanyProfileMissingOrOutdated(companyProfile);
        if (companyProfileNeedsRefresh) {
            companyProfile = stockProviderPort.getCompanyProfile(ticker);
        }
        if (companyProfile == null) {
            log.warn("No company profile found for ticker {}, skipping", ticker);
            return;
        }
        String prohibitionReason = resolveProhibitionReason(companyProfile, prohibitedKeywords);
        if (prohibitionReason != null) {
            ProhibitedTicker newProhibitedTicker = ProhibitedTicker.createProhibited(ticker,
                    prohibitionReason);
            prohibitedTickerRepository.save(newProhibitedTicker);
            log.info("Ticker {} marked as prohibited based on company profile by '{}'", ticker,
                    prohibitionReason);
            return;
        }
        validTickers.add(ticker);
        if (companyProfileNeedsRefresh) {
            companyProfileRepository.save(companyProfile);
        }
        log.info("Company profile for ticker {} saved/updated successfully", ticker);
    }

    private String resolveProhibitionReason(CompanyProfile companyProfile,
            List<ProhibitedKeyword> prohibitedKeywords) {
        return prohibitedKeywordMatcher.findProhibitionReason(companyProfile.getName(), prohibitedKeywords);
    }

    @Override
    public CandleChartDTO findCandlesByStockId(Long id) {
        Stock stock = stockDataRepository.findById(id)
                .orElseThrow(() -> new StockDataNotFoundException(TICKER_DATA_NOT_FOUND + id));
        List<Candle> candles = candleHistoryRepository.findCandlesByTicker(stock.getTicker());
        return candleDTOMapper.toChartDTO(stock, candles);
    }

    @Override
    public boolean getValorationIA(Long id) {
        Stock stock = stockDataRepository.findById(id)
                .orElseThrow(() -> new StockDataNotFoundException(TICKER_DATA_NOT_FOUND + id));
        String ticker = stock.getTicker();
        String prompt = enforcePromptSize(
                promptBuilder.buildAnalysisPrompt(stock, stock.getStrategyEvaluation()),
                ticker,
                "initial");

        String valoration = resolveValorationWithValidation(prompt, ticker);
        log.info("AI valoration stored for ticker={} responseLength={} fallbackUsed={}",
                ticker,
                valoration == null ? 0 : valoration.length(),
                IA_FALLBACK_VALORATION.equals(valoration));
        stock.setValorationIA(valoration);
        stockDataRepository.save(stock);
        return !IA_FALLBACK_VALORATION.equals(valoration);
    }

    private String resolveValorationWithValidation(String prompt, String ticker) {
        aiRequests.incrementAndGet();
        try {
            String valoration = apiIAPort.getValoration(prompt);
            if (promptResponseValidator.isValid(valoration)) {
                aiValidResponses.incrementAndGet();
                logAiMetrics();
                return valoration;
            }

            log.warn("Invalid AI valoration format for ticker {}, retrying with strict prompt", ticker);
            aiRetries.incrementAndGet();
            String retryPrompt = enforcePromptSize(
                    promptResponseValidator.buildRetryPrompt(prompt),
                    ticker,
                    "retry");
            String retryValoration = apiIAPort.getValoration(retryPrompt);
            if (promptResponseValidator.isValid(retryValoration)) {
                aiValidResponses.incrementAndGet();
                logAiMetrics();
                return retryValoration;
            }

            log.warn("Invalid AI valoration format for ticker {} after retry, using fallback", ticker);
            aiFallbacks.incrementAndGet();
            logAiMetrics();
            return IA_FALLBACK_VALORATION;
        } catch (RuntimeException ex) {
            aiFallbacks.incrementAndGet();
            log.error("Error getting AI valoration for ticker {}, using fallback", ticker, ex);
            logAiMetrics();
            return IA_FALLBACK_VALORATION;
        }
    }

    private String enforcePromptSize(String prompt, String ticker, String stage) {
        String safePrompt = Objects.requireNonNull(prompt, "Prompt cannot be null");
        if (safePrompt.length() <= MAX_PROMPT_CHARS) {
            return safePrompt;
        }
        log.warn("AI prompt truncated for ticker={} stage={} originalLength={} truncatedLength={}",
                ticker, stage, safePrompt.length(), MAX_PROMPT_CHARS);
        return safePrompt.substring(0, MAX_PROMPT_CHARS);
    }

    private void logAiMetrics() {
        long requests = aiRequests.get();
        long valid = aiValidResponses.get();
        long retries = aiRetries.get();
        long fallbacks = aiFallbacks.get();

        double validRatio = requests == 0 ? 0.0d : (double) valid / requests;
        double retryRatio = requests == 0 ? 0.0d : (double) retries / requests;
        double fallbackRatio = requests == 0 ? 0.0d : (double) fallbacks / requests;

        log.info(
                "ai_valoration_metrics requests={} valid={} retries={} fallbacks={} validRatio={} retryRatio={} fallbackRatio={}",
                requests,
                valid,
                retries,
                fallbacks,
                formatRatio(validRatio),
                formatRatio(retryRatio),
                formatRatio(fallbackRatio));
    }

    private String formatRatio(double ratio) {
        return String.format(Locale.ROOT, "%.2f", ratio);
    }
}
