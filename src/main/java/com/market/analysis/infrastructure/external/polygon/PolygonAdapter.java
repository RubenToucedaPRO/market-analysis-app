package com.market.analysis.infrastructure.external.polygon;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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
import com.market.analysis.domain.model.HistoricalData;
import com.market.analysis.domain.port.out.HistoricalProviderPort;
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

    private static final long RATE_LIMIT_WINDOW = 62000; // 1 minuto + margen
    private static final int MAX_CALLS_PER_MINUTE = 5;
    private static final int SIZE_HISTORICAL = 300;

    /**
     * Deque para rastrear timestamps de llamadas y cumplir el rate limit en
     * memoria.
     */
    private final Deque<Instant> apiCallTimestamps = new ConcurrentLinkedDeque<>();

    @Override
    public HistoricalData fetchHistoricalData(String ticker) {
        log.debug("Solicitando datos históricos a Polygon para: {}", ticker);

        // 1. Control de flujo (Responsabilidad técnica del adaptador)
        waitForRateLimit();

        // 2. Construcción de la URI
        URI uri = buildUri(ticker, SIZE_HISTORICAL);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            // Registrar llamada exitosa para el rate limit
            recordApiCall();

            // 3. Mapeo de respuesta JSON (Infraestructura) a modelo de Dominio
            return mapToHistoricalData(ticker, response.getBody());

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.error("Rate limit exceeded (429) for ticker {}", ticker, e);
                throw new PolygonException("Rate limit exceeded for " + ticker, e);
            }
            log.error("HTTP client error communicating with Polygon for {}: {}", ticker, e.getStatusCode(), e);
            throw new PolygonException("HTTP error communicating with Polygon for " + ticker + ": " + e.getStatusCode(), e);
        } catch (PolygonException e) {
            // Re-throw domain exceptions without wrapping
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing Polygon data for {}", ticker, e);
            throw new PolygonException("Unexpected error processing data for " + ticker, e);
        }
    }

    private HistoricalData mapToHistoricalData(String ticker, String jsonBody) {
        List<Double> prices = new ArrayList<>();
        List<Long> volumes = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonBody);
            JsonNode resultsNode = root.path("results");

            if (resultsNode.isArray()) {
                for (JsonNode node : resultsNode) {
                    // 'c' = close price, 'v' = volume en la API de Polygon
                    prices.add(node.path("c").asDouble());
                    volumes.add(node.path("v").asLong());

                }
            }
        } catch (Exception e) {
            log.error("Error parsing JSON response for ticker {}", ticker, e);
            throw new PolygonException("Error parsing JSON response for " + ticker, e);
        }

        return new HistoricalData(ticker, prices, volumes, Instant.now());
    }

    private URI buildUri(String ticker, int size) {
        LocalDate toDate = LocalDate.now();
        // Se restan 350 días para asegurar que obtenemos suficientes días bursátiles
        // para una SMA200
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
                        log.error("Thread interrupted while waiting for rate limit", e);
                        throw new PolygonException("Interrupted while waiting for rate limit", e);
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