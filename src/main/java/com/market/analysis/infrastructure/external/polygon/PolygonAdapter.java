package com.market.analysis.infrastructure.external.polygon;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.analysis.domain.model.Candle;
import com.market.analysis.domain.model.HistoricalData;
import com.market.analysis.domain.port.out.HistoricalProviderPort;
import com.market.analysis.infrastructure.exception.PolygonException;
import com.market.analysis.infrastructure.persistence.repository.SqlCandleHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolygonAdapter implements HistoricalProviderPort {

    @Value("${polygon.api.token:}")
    private String apiToken;

    @Value("${polygon.base.url:}")
    private String baseUrl;

    @Qualifier("polygonRestTemplate")
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SqlCandleHistoryRepository candleHistoryRepository;

    private static final long RATE_LIMIT_WINDOW = 62000; // 1 minute + margin
    private static final int MAX_CALLS_PER_MINUTE = 5;
    private static final int SIZE_HISTORICAL = 300;

    /**
     * Deque to track call timestamps and enforce rate limiting in memory.
     */
    private final Deque<Instant> apiCallTimestamps = new ConcurrentLinkedDeque<>();

    /**
     * Holds both the HistoricalData (for indicator calculations) and the
     * parsed candle list (for persistence) from a single JSON parse pass.
     */
    private record ParseResult(HistoricalData historicalData, List<Candle> candles) {}

    @Override
    public HistoricalData fetchHistoricalData(String ticker) {
        log.debug("Requesting historical data from Polygon for: {}", ticker);

        // 1. Flow control (Adapter technical responsibility)
        waitForRateLimit();

        // 2. URI construction
        URI uri = buildUri(ticker, SIZE_HISTORICAL);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            // Record successful call for rate limiting
            recordApiCall();

            // 3. JSON response mapping (Infrastructure) to Domain model
            ParseResult parseResult = parseApiResponse(ticker, response.getBody());

            // 4. Persist candle history (F1.7)
            persistCandles(ticker, parseResult.candles());

            return parseResult.historicalData();

        } catch (PolygonException e) {
            // Re-throw PolygonException as-is to preserve the original message
            throw e;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.warn("Rate limit reached (429) for {}", ticker);
            }
            throw new PolygonException("Error communicating with Polygon: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new PolygonException("Unexpected error processing Polygon data for " + ticker, e);
        }
    }

    /**
     * Parses the Polygon JSON response and extracts both the HistoricalData
     * (closing prices + volumes for indicator calculations) and a full list of
     * OHLCV candles for persistence.
     *
     * <p>Candles are only created when the result node contains a valid
     * {@code t} timestamp field (epoch milliseconds &gt; 0). Nodes with no
     * timestamp are still counted toward {@code closingPrices} and
     * {@code volumes} to preserve the existing contract.</p>
     */
    private ParseResult parseApiResponse(String ticker, String jsonBody) {
        List<Double> prices = new ArrayList<>();
        List<Long> volumes = new ArrayList<>();
        List<Candle> candles = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonBody);
            JsonNode resultsNode = root.path("results");

            if (resultsNode.isArray()) {
                for (JsonNode node : resultsNode) {
                    // 'c' = close price, 'v' = volume en la API de Polygon
                    double closePrice = node.path("c").asDouble();
                    long volume = node.path("v").asLong();
                    prices.add(closePrice);
                    volumes.add(volume);

                    // F1.6: extract full OHLCV + timestamp for candle persistence
                    long timestampMs = node.path("t").asLong();
                    if (timestampMs > 0) {
                        candles.add(buildCandle(ticker, node, closePrice, volume, timestampMs));
                    }
                }
            }
        } catch (Exception e) {
            throw new PolygonException("Error parsing API response for ticker " + ticker, e);
        }

        return new ParseResult(new HistoricalData(ticker, prices, volumes, Instant.now()), candles);
    }

    private Candle buildCandle(String ticker, JsonNode node, double closePrice, long volume, long timestampMs) {
        return Candle.builder()
                .ticker(ticker)
                .dateTime(Instant.ofEpochMilli(timestampMs))
                .openPrice(BigDecimal.valueOf(node.path("o").asDouble()))
                .highPrice(BigDecimal.valueOf(node.path("h").asDouble()))
                .lowPrice(BigDecimal.valueOf(node.path("l").asDouble()))
                .closePrice(BigDecimal.valueOf(closePrice))
                .volume(volume)
                .build();
    }

    /**
     * Persists the parsed candles and logs observability data (F1.7 + F1.8).
     */
    private void persistCandles(String ticker, List<Candle> candles) {
        if (candles.isEmpty()) {
            log.debug("persistCandles: no candles with timestamp to persist for ticker={}", ticker);
            return;
        }

        Optional<Instant> minDate = candles.stream().map(Candle::getDateTime).min(Comparator.naturalOrder());
        Optional<Instant> maxDate = candles.stream().map(Candle::getDateTime).max(Comparator.naturalOrder());

        log.info("persistCandles: persisting {} candle(s) for ticker={} from={} to={}",
                candles.size(), ticker, minDate.orElse(null), maxDate.orElse(null));

        candleHistoryRepository.saveCandlesForTicker(ticker, candles);
    }

    private URI buildUri(String ticker, int size) {
        LocalDate toDate = LocalDate.now();
        // Subtract 350 days to ensure we get enough trading days for SMA200
        LocalDate fromDate = toDate.minusDays(350);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("v2/aggs/ticker/{ticker}/range/1/day/{from}/{to}")
                .queryParam("adjusted", "true")
                .queryParam("sort", "desc")
                .queryParam("limit", size)
                .queryParam("apiKey", apiToken)
                .buildAndExpand(ticker.toUpperCase(), fromDate.format(formatter), toDate.format(formatter))
                .toUri();
    }

    private void waitForRateLimit() {
        removeExpiredTimestamps();
        if (apiCallTimestamps.size() >= MAX_CALLS_PER_MINUTE) {
            Instant oldestCall = apiCallTimestamps.peekFirst();
            if (oldestCall != null) {
                long waitTime = oldestCall.toEpochMilli()
                        - (Instant.now().minusMillis(RATE_LIMIT_WINDOW).toEpochMilli()) + 100;
                if (waitTime > 0) {
                    try {
                        log.info("Rate limit reached. Waiting {}ms", waitTime);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    removeExpiredTimestamps();
                }
            }
        }
    }

    private void recordApiCall() {
        apiCallTimestamps.addLast(Instant.now());
    }

    private void removeExpiredTimestamps() {
        Instant windowStart = Instant.now().minusMillis(RATE_LIMIT_WINDOW);
        while (!apiCallTimestamps.isEmpty() && apiCallTimestamps.peekFirst().isBefore(windowStart)) {
            apiCallTimestamps.pollFirst();
        }
    }

}