package com.market.analysis.infrastructure.external.polygon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.analysis.domain.model.ApiCallLog;
import com.market.analysis.domain.port.out.ApiCallRateRepository;
import com.market.analysis.infrastructure.exception.PolygonException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class PolygonAdapter {
    @Value("${polygon.api.token:}")
    private String apiToken;

    @Value("${polygon.base.url:}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    private final ApiCallRateRepository apiCallRateRepository;

    private static final ZoneId EASTERN_TIME = ZoneId.of("America/New_York");

    private static final long RATE_LIMIT_WINDOWS = 62000; // 1 minute in milliseconds

    private static final int AV_VOLUMEN = 20; // Period for average volume calculation

    private static final int MAX_CALLS_PER_MINUTE = 5; // Polygon.io free tier limit

    /**
     * Thread-safe deque to track timestamps of API calls within the rate limit
     * window
     */
    private final Deque<Instant> apiCallTimestamps = new ConcurrentLinkedDeque<>();

    /**
     * Fetches the last 200 daily candles from Polygon.io and calculates SMA20,
     * SMA50, SMA200 locally, along with current volume and average volume.
     * Consumes 1 API call per ticker instead of 3.
     */
    public SmaValues fetchAllSmaValues(String ticker) {
        log.info("Updating SMAs for {} using Polygon.io (Single Call)", ticker);

        if (!canUpdateTicker(ticker)) {
            throw new PolygonException("El ticker " + ticker
                    + " ya fue actualizado hoy desde Polygon.io.");
        }

        // 1. Fetch 200 days of history (Minimum needed for SMA200)
        CandleDataResult candleResult = fetchCandleDataPolygon(ticker, 200);

        if (candleResult == null || candleResult.closingPrices().isEmpty()
                || candleResult.closingPrices().size() < 20) {
            log.error(
                    "Insufficient data for {}: retrieved {} records, need at least 20 to compute SMAs. Partial SMAs are only calculated when 20–199 records are available.",
                    ticker, candleResult != null ? candleResult.closingPrices().size() : 0);
            return null;
        }
        Double sma20 = 0.0;
        Double sma50 = 0.0;
        Double sma200 = 0.0;

        // 2. Calculate SMAs locally
        if (candleResult.closingPrices().size() >= 20) {
            sma20 = calculateAverage(candleResult.closingPrices(), 20);
        }
        if (candleResult.closingPrices().size() >= 50) {
            sma50 = calculateAverage(candleResult.closingPrices(), 50);
        }
        if (candleResult.closingPrices().size() >= 200) {
            sma200 = calculateAverage(candleResult.closingPrices(), 200);
        }

        // 3. Extract current volume (most recent candle)
        Long currentVolume = null;
        if (!candleResult.volumes().isEmpty()) {
            currentVolume = candleResult.volumes().get(0); // Most recent (index 0 due to DESC sort)
        }

        // 4. Calculate average volume using period from settings
        Long averageVolume = calculateAverageVolume(candleResult.volumes(),
                AV_VOLUMEN);

        if (sma20 != null) {
            recordUpdate(ticker);
        }

        // 5. Return object with SMAs, volume, and average volume
        return new SmaValues(sma20, sma50, sma200, currentVolume, averageVolume);
    }

    /*
     * Fetches candle data (closing prices and volumes) from Polygon.io API.
     * Uses date range (from/to) to fetch historical data.
     * Polygon free tier allows 5 API calls per minute.
     */
    private CandleDataResult fetchCandleDataPolygon(String symbol, int size) {
        log.debug("Fetching candle data for symbol: {}", symbol);

        waitForRateLimit();

        // Calculate date range (approximately 300 calendar days to get ~200 trading
        // days)
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(300);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String endpoint = String.format("v2/aggs/ticker/%s/range/1/day/%s/%s",
                symbol.toUpperCase(),
                fromDate.format(formatter),
                toDate.format(formatter));

        URI uri = UriComponentsBuilder.fromUriString(baseUrl + endpoint)
                .queryParam("adjusted", "true")
                .queryParam("sort", "desc") // Most recent first (consistent with Twelve Data)
                .queryParam("limit", size) // Limit results to what we need
                .queryParam("apiKey", apiToken)
                .build()
                .toUri();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            recordApiCall();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            // Check for error status
            String status = root.path("status").asText();
            if ("ERROR".equalsIgnoreCase(status) || "NOT_FOUND".equalsIgnoreCase(status)) {
                log.error("Polygon.io Error for {}: {}", symbol, root.path("error").asText());
                return new CandleDataResult(Collections.emptyList(), Collections.emptyList());
            }

            // Extract results array
            JsonNode resultsNode = root.path("results");
            if (resultsNode.isMissingNode() || !resultsNode.isArray()) {
                log.warn("No 'results' array found for {}", symbol);
                return new CandleDataResult(Collections.emptyList(), Collections.emptyList());
            }

            List<Double> prices = new ArrayList<>();
            List<Long> volumes = new ArrayList<>();
            // Polygon.io returns results with 'c' field for close price and 'v' for volume
            // Order is DESC (newest first) due to sort=desc parameter
            for (JsonNode node : resultsNode) {
                prices.add(node.path("c").asDouble());
                volumes.add(node.path("v").asLong());
            }

            return new CandleDataResult(prices, volumes);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.warn("Rate limit exceeded (429) for {}", symbol);
            } else {
                log.error("HTTP Error {}: {}", e.getStatusCode(), e.getMessage());
            }
            return new CandleDataResult(Collections.emptyList(), Collections.emptyList());
        } catch (Exception e) {
            log.error("Error parsing Polygon.io response for {}: {}", symbol, e.getMessage());
            return new CandleDataResult(Collections.emptyList(), Collections.emptyList());
        }
    }

    /*
     * Calculates the simple average of the first 'period' closing prices from the
     * list.
     * Assumes prices are in DESC order (most recent first).
     */
    private Double calculateAverage(List<Double> prices, int period) {
        if (prices == null || prices.size() < period)
            return null;
        double sum = 0;
        for (int i = 0; i < period; i++)
            sum += prices.get(i);
        double average = sum / period;

        return BigDecimal.valueOf(average)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /*
     * Calculates the simple average of the first 'period' volumes from the list.
     * Assumes volumes are in DESC order (most recent first).
     */
    private Long calculateAverageVolume(List<Long> volumes, int period) {
        if (volumes == null || volumes.size() < period)
            return null;
        long sum = 0;
        for (int i = 0; i < period; i++)
            sum += volumes.get(i);
        return sum / period;
    }

    /**
     * Waits if necessary to comply with the rate limit.
     * Removes expired timestamps and checks if we've reached the maximum calls per
     * minute.
     * If the limit is reached, waits until the oldest call expires from the window.
     */
    private void waitForRateLimit() {
        removeExpiredTimestamps();

        int maxCalls = MAX_CALLS_PER_MINUTE;

        if (apiCallTimestamps.size() >= maxCalls) {
            Instant oldestCall = apiCallTimestamps.peekFirst();
            if (oldestCall != null) {
                long waitTimeMs = calculateWaitTime(oldestCall);
                if (waitTimeMs > 0) {
                    log.info("Rate limit reached ({} calls/min). Waiting {} ms before next API call.",
                            maxCalls, waitTimeMs);
                    sleepForRateLimit(waitTimeMs);
                    removeExpiredTimestamps();
                }
            }
        }
    }

    /**
     * Calculates the time to wait based on the oldest API call timestamp.
     *
     * @param oldestCall the timestamp of the oldest API call in the window
     * @return the time to wait in milliseconds, or 0 if no wait is needed
     */
    private long calculateWaitTime(Instant oldestCall) {
        long rateLimitWindow = RATE_LIMIT_WINDOWS;
        Instant windowStart = Instant.now().minusMillis(rateLimitWindow);
        if (oldestCall.isAfter(windowStart)) {
            return oldestCall.toEpochMilli() - windowStart.toEpochMilli() + 100;
        }
        return 0;
    }

    /**
     * Sleeps for the specified duration to respect rate limiting.
     *
     * @param waitTimeMs the time to wait in milliseconds
     */
    private void sleepForRateLimit(long waitTimeMs) {
        try {
            Thread.sleep(waitTimeMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Rate limit wait interrupted");
        }
    }

    /**
     * Records the current timestamp as an API call.
     */
    private void recordApiCall() {
        apiCallTimestamps.addLast(Instant.now());
    }

    /**
     * Removes timestamps that are older than the rate limit window.
     */
    private void removeExpiredTimestamps() {
        long rateLimitWindow = RATE_LIMIT_WINDOWS;
        Instant windowStart = Instant.now().minusMillis(rateLimitWindow);
        while (!apiCallTimestamps.isEmpty() && apiCallTimestamps.peekFirst().isBefore(windowStart)) {
            apiCallTimestamps.pollFirst();
        }
    }

    /**
     * Record containing the three SMA values and volume data.
     */
    public record SmaValues(Double sma20, Double sma50, Double sma200, Long volume, Long averageVolume) {
        public boolean hasAllValues() {
            return sma20 != null && sma50 != null && sma200 != null;
        }

        public boolean hasAnyValue() {
            return sma20 != null || sma50 != null || sma200 != null;
        }
    }

    /**
     * Record containing candle data (closing prices and volumes).
     */
    private record CandleDataResult(List<Double> closingPrices, List<Long> volumes) {
    }

    /**
     * Checks if a ticker can be updated today.
     * Returns false if already updated today or if daily limit is reached.
     *
     * @param ticker the stock ticker symbol
     * @return true if the ticker can be updated
     */
    public boolean canUpdateTicker(String ticker) {
        // Check if this ticker was already updated today
        if (wasUpdatedToday(ticker)) {
            log.info("Ticker {} was already updated today, skipping", ticker);
            return false;
        }

        return true;
    }

    /**
     * Checks if a ticker was already updated today from Polygon.io.
     * Does not consider the daily limit - only checks the last update date.
     *
     * @param ticker the stock ticker symbol
     * @return true if the ticker was updated today
     */
    public boolean wasUpdatedToday(String ticker) {
        Optional<ApiCallLog> logEntry = apiCallRateRepository.findByTicker(ticker);
        if (logEntry.isPresent()) {
            LocalDate ocurredAt = logEntry.get().getOcurredAt();
            LocalDate today = LocalDate.now(EASTERN_TIME);

            if (ocurredAt != null && ocurredAt.equals(today)) {
                log.debug("Ticker {} was already updated today from Polygon.io ({})", ticker, today);
                return true;
            }
        }
        return false;
    }

    /**
     * Records an update in the database log.
     *
     * @param ticker the stock ticker symbol
     */
    public void recordUpdate(String ticker) {
        LocalDate today = LocalDate.now(EASTERN_TIME);

        Optional<ApiCallLog> existingLog = apiCallRateRepository.findByTicker(ticker);

        ApiCallLog apiResponseLogger = existingLog
                .orElseGet(() -> ApiCallLog.builder()
                        .ticker(ticker)
                        .ocurredAt(today.atStartOfDay(EASTERN_TIME).toLocalDate())
                        .build());

        apiResponseLogger.setOcurredAt(today);
        apiCallRateRepository.save(apiResponseLogger.getTicker(), apiResponseLogger.getOcurredAt()
                .atStartOfDay(EASTERN_TIME).toLocalDate().atStartOfDay(EASTERN_TIME).toLocalDate());
        log.debug("Recorded Polygon.io update for ticker {} on {}", ticker, today);
    }

    /* Delete log entry for a ticker */
    public void deleteUpdateLogByTicker(String ticker) {
        apiCallRateRepository.deleteByTicker(ticker);
        log.debug("Deleted Polygon.io update log for ticker {}", ticker);
    }
}
