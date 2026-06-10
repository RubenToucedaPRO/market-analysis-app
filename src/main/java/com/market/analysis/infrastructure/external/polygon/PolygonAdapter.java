package com.market.analysis.infrastructure.external.polygon;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
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
import com.market.analysis.infrastructure.config.ApiConstants;
import com.market.analysis.infrastructure.exception.PolygonException;

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

    private static final long RATE_LIMIT_WINDOW = 62000; // 1 minute + margin
    private static final int MAX_CALLS_PER_MINUTE = 5;

    /**
     * Deque to track call timestamps and enforce rate limiting in memory.
     */
    private final Deque<Instant> apiCallTimestamps = new ConcurrentLinkedDeque<>();

    private static final int SIZE_HISTORICAL = 300;

    @Override
    public HistoricalData fetchHistoricalData(String ticker) {
        log.debug("Requesting historical data from Polygon for: {}", ticker);

        waitForRateLimit();

        URI uri = buildUri(ticker, SIZE_HISTORICAL);

        try {
            // Record successful call for rate limiting
            recordApiCall();
            
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            
            //  JSON response mapping (Infrastructure) to Domain model.
            //    Candle persistence is orchestrated by the Application Use Case.
            return parseApiResponse(ticker, response.getBody());

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
     * Parses the Polygon JSON response into a {@link HistoricalData} object that
     * contains both closing prices / volumes (for technical indicator calculations)
     * and the full OHLCV candle list (for persistence by the Application layer).
     *
     * <p>Candles are only created when the result node contains a valid
     * {@code t} timestamp field (epoch milliseconds &gt; 0). Nodes with no
     * timestamp are still counted toward {@code closingPrices} and
     * {@code volumes} to preserve the existing contract.</p>
     */
    private HistoricalData parseApiResponse(String ticker, String jsonBody) {
        List<Double> prices = new ArrayList<>();
        List<Long> volumes = new ArrayList<>();
        List<Candle> candles = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonBody);
            JsonNode resultsNode = root.path(ApiConstants.JSON_RESULTS);

            if (resultsNode.isArray()) {
                for (JsonNode node : resultsNode) {
                    // 'c' = close price, 'v' = volume en la API de Polygon
                    double closePrice = node.path(ApiConstants.JSON_CLOSE).asDouble();
                    long volume = node.path(ApiConstants.JSON_VOLUME).asLong();
                    prices.add(closePrice);
                    volumes.add(volume);

                    // F1.6: extract full OHLCV + timestamp into domain Candle
                    long timestampMs = node.path(ApiConstants.JSON_TIMESTAMP).asLong();
                    if (timestampMs > 0) {
                        candles.add(buildCandle(ticker, node, closePrice, volume, timestampMs));
                    }
                }
            }
        } catch (Exception e) {
            throw new PolygonException("Error parsing API response for ticker " + ticker, e);
        }

        return HistoricalData.builder()
                .ticker(ticker)
                .closingPrices(prices)
                .volumes(volumes)
                .lastUpdate(Instant.now())
                .candles(candles)
                .build();
    }

    private Candle buildCandle(String ticker, JsonNode node, double closePrice, long volume, long timestampMs) {
        return Candle.builder()
                .ticker(ticker)
                .dateTime(Instant.ofEpochMilli(timestampMs))
                .openPrice(BigDecimal.valueOf(node.path(ApiConstants.JSON_OPEN).asDouble()))
                .highPrice(BigDecimal.valueOf(node.path(ApiConstants.JSON_HIGH).asDouble()))
                .lowPrice(BigDecimal.valueOf(node.path(ApiConstants.JSON_LOW).asDouble()))
                .closePrice(BigDecimal.valueOf(closePrice))
                .volume(volume)
                .build();
    }

    private URI buildUri(String ticker, int size) {
        LocalDate toDate = LocalDate.now();
        // Subtract 350 days to ensure we get enough trading days for SMA200
        LocalDate fromDate = toDate.minusDays(350);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ApiConstants.POLYGON_DATE_PATTERN);

        return UriComponentsBuilder.fromUriString(baseUrl)
                .path(ApiConstants.POLYGON_URI_AGGREGATES)
                .queryParam(ApiConstants.POLYGON_QUERY_ADJUSTED, "true")
                .queryParam(ApiConstants.POLYGON_QUERY_SORT, ApiConstants.POLYGON_SORT_DESC)
                .queryParam(ApiConstants.POLYGON_QUERY_LIMIT, size)
                .queryParam(ApiConstants.POLYGON_QUERY_API_KEY, apiToken)
                .buildAndExpand(ticker.toUpperCase(), fromDate.format(formatter), toDate.format(formatter))
                .toUri();
    }

    private synchronized void waitForRateLimit() {
        removeExpiredTimestamps();
        
        // We fetch the oldest call snapshot to evaluate the loop condition cleanly
        Instant oldestCall = apiCallTimestamps.peekFirst();
        
        while (apiCallTimestamps.size() >= MAX_CALLS_PER_MINUTE && oldestCall != null) {
            long elapsed = Instant.now().toEpochMilli() - oldestCall.toEpochMilli();
            long waitTime = RATE_LIMIT_WINDOW - elapsed;
            
            // If the window time has already passed, we can break naturally by clearing the condition
            if (waitTime > 0) {
                try {
                    log.info("Polygon rate limit window saturated ({} calls). Waiting {}ms for safety...", apiCallTimestamps.size(), waitTime);
                    this.wait(waitTime + 200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return; // Standard pattern for handling InterruptedException
                }
            }
            
            // Refresh state for the next loop iteration check
            removeExpiredTimestamps();
            oldestCall = apiCallTimestamps.peekFirst();
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