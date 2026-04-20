package com.market.analysis.application.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import com.market.analysis.domain.model.Candle;
import com.market.analysis.domain.model.HistoricalData;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StockOrigin;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.model.TechnicalIndicators;
import com.market.analysis.domain.port.out.ApiCallRateRepository;
import com.market.analysis.domain.port.out.CandleHistoryRepository;
import com.market.analysis.domain.port.out.HistoricalProviderPort;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
import com.market.analysis.domain.service.EvaluateStrategyService;
import com.market.analysis.domain.service.StockHistoricalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class StockDeterministicAnalysisPipeline {

    private static final ZoneId NEW_YORK_ZONE = ZoneId.of("America/New_York");
    private static final int DEFAULT_INDICATOR_PERIOD = 20;

    private final StockDataRepository stockDataRepository;
    private final StrategyEvaluationRepository strategyEvaluationRepository;
    private final ApiCallRateRepository apiCallRateRepository;
    private final CandleHistoryRepository candleHistoryRepository;
    private final StockProviderPort stockProviderPort;
    private final HistoricalProviderPort historicalProviderPort;
    private final StockHistoricalService stockHistoricalService;
    private final EvaluateStrategyService evaluateStrategyService;

    public Stock analyzeAndPersist(String ticker, Strategy strategy, StockOrigin origin) {
        Stock stock = getDataFromProvider(ticker);
        if (stock == null) {
            return null;
        }

        stock.setStrategyId(strategy.getId());
        stock.setOrigin(origin);

        Stock savedStock = stockDataRepository.save(stock);
        StrategyEvaluation evaluationResult = evaluateStrategyService.evaluateStrategy(strategy, savedStock);
        if (evaluationResult != null) {
            strategyEvaluationRepository.save(evaluationResult, savedStock);
            log.info("Ticker {} added with strategy '{}' (origin={}): {}",
                    ticker,
                    strategy.getName(),
                    origin,
                    evaluationResult.isCompliant() ? "PASSED" : "FAILED");
        } else {
            log.warn("No strategy evaluation generated for ticker {} and strategy '{}'", ticker, strategy.getName());
        }

        return savedStock;
    }

    private Stock getDataFromProvider(String ticker) {
        Stock stock = stockProviderPort.getQuote(ticker);
        if (stock == null) {
            log.warn("No stock quote found for ticker: {}", ticker);
            return null;
        }

        Instant startOfToday = LocalDate.now(NEW_YORK_ZONE)
                .atStartOfDay(NEW_YORK_ZONE)
                .toInstant();
        Instant startOfTomorrow = LocalDate.now(NEW_YORK_ZONE)
                .plusDays(1)
                .atStartOfDay(NEW_YORK_ZONE)
                .toInstant();

        Stock existingStock = stockDataRepository.findByTickerAndLastUpdateBetween(ticker, startOfToday,
                startOfTomorrow);

        if (existingStock != null) {
            log.info("Using historical existing stock data for ticker: {}", ticker);
            applyCachedDailyMetrics(stock, existingStock);
        } else {
            enrichWithFreshHistoricalIndicators(ticker, stock);
        }

        return stock;
    }

    private void applyCachedDailyMetrics(Stock targetStock, Stock cachedStock) {
        targetStock.setSma20(cachedStock.getSma20());
        targetStock.setSma50(cachedStock.getSma50());
        targetStock.setSma200(cachedStock.getSma200());
        targetStock.setVolume(cachedStock.getVolume());
        targetStock.setAverageVolume(cachedStock.getAverageVolume());
        targetStock.setLastUpdated(cachedStock.getLastUpdated());
        targetStock.setEma9(cachedStock.getEma9());
        targetStock.setEma12(cachedStock.getEma12());
        targetStock.setEma20(cachedStock.getEma20());
        targetStock.setEma26(cachedStock.getEma26());
        targetStock.setEma50(cachedStock.getEma50());
        targetStock.setEma200(cachedStock.getEma200());
        targetStock.setRsi14(cachedStock.getRsi14());
        targetStock.setRsi30(cachedStock.getRsi30());
        targetStock.setMacdLine(cachedStock.getMacdLine());
        targetStock.setMacdSignal(cachedStock.getMacdSignal());
        targetStock.setMacdHistogram(cachedStock.getMacdHistogram());
        targetStock.setBbUpper20(cachedStock.getBbUpper20());
        targetStock.setBbLower20(cachedStock.getBbLower20());
        targetStock.setAtr14(cachedStock.getAtr14());
    }

    private void enrichWithFreshHistoricalIndicators(String ticker, Stock stock) {
        log.info("Fetching new stock data for ticker: {}", ticker);
        HistoricalData historicalData = historicalProviderPort.fetchHistoricalData(ticker);
        if (historicalData == null) {
            log.warn("No historical data found for ticker: {}, skipping technical indicators", ticker);
            return;
        }

        apiCallRateRepository.save(ticker, historicalData.getLastUpdate());
        log.info("Historical data for ticker {} fetched and saved successfully", ticker);
        persistCandlesIfPresent(ticker, historicalData.getCandles());

        TechnicalIndicators technicalIndicators = stockHistoricalService.calculateIndicators(historicalData,
                DEFAULT_INDICATOR_PERIOD);
        if (technicalIndicators == null) {
            log.warn("No technical indicators calculated for ticker: {}", ticker);
            return;
        }

        applyTechnicalIndicators(stock, technicalIndicators);
    }

    private void persistCandlesIfPresent(String ticker, List<Candle> candles) {
        if (candles == null || candles.isEmpty()) {
            log.debug("No candles to persist for ticker={}", ticker);
            return;
        }
        log.info("Persisting {} candle(s) for ticker={}", candles.size(), ticker);
        candleHistoryRepository.saveCandlesForTicker(ticker, candles);
    }

    private void applyTechnicalIndicators(Stock stock, TechnicalIndicators technicalIndicators) {
        stock.setSma20(technicalIndicators.getSma20());
        stock.setSma50(technicalIndicators.getSma50());
        stock.setSma200(technicalIndicators.getSma200());
        stock.setVolume(technicalIndicators.getCurrentVolume());
        stock.setAverageVolume(technicalIndicators.getAverageVolume());
        stock.setLastUpdated(technicalIndicators.getLastUpdated());

        stock.setEma9(technicalIndicators.getEma9());
        stock.setEma12(technicalIndicators.getEma12());
        stock.setEma20(technicalIndicators.getEma20());
        stock.setEma26(technicalIndicators.getEma26());
        stock.setEma50(technicalIndicators.getEma50());
        stock.setEma200(technicalIndicators.getEma200());

        stock.setRsi14(technicalIndicators.getRsi14());
        stock.setRsi30(technicalIndicators.getRsi30());

        stock.setMacdLine(technicalIndicators.getMacdLine());
        stock.setMacdSignal(technicalIndicators.getMacdSignal());
        stock.setMacdHistogram(technicalIndicators.getMacdHistogram());

        stock.setBbUpper20(technicalIndicators.getBbUpper20());
        stock.setBbLower20(technicalIndicators.getBbLower20());

        stock.setAtr14(technicalIndicators.getAtr14());
    }
}
