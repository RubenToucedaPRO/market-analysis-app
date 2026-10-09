package com.market.analysis.infrastructure.external.finnhub;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.market.analysis.infrastructure.external.shared.SlidingWindowThrottler;

/**
 * Explicit in-memory throttler for Finnhub API calls.
 *
 * <p>Thin provider binding over {@link SlidingWindowThrottler}: both
 * {@code getQuote} and {@code getCompanyProfile} share this single bean, so
 * the configured budget covers every Finnhub call. Replaces the Resilience4j
 * {@code @RateLimiter} annotation, which stayed silent in Docker because its
 * configuration never reached the packaged jar.</p>
 */
@Component
public class FinnhubThrottler extends SlidingWindowThrottler {

    public FinnhubThrottler(
            @Value("${finnhub.ratelimit.max-calls:55}") int maxCallsPerWindow,
            @Value("${finnhub.ratelimit.window-ms:65000}") long windowMs) {
        super("Finnhub", maxCallsPerWindow, windowMs);
    }
}
