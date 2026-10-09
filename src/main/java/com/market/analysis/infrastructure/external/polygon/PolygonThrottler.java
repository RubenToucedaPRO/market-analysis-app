package com.market.analysis.infrastructure.external.polygon;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.market.analysis.infrastructure.external.shared.SlidingWindowThrottler;

/**
 * Explicit in-memory throttler for Polygon API calls.
 *
 * <p>Thin provider binding over {@link SlidingWindowThrottler}: a single shared
 * bean owns the per-minute budget (5 calls/minute by default), so every
 * historical-data fetch goes through the same bucket. Callers just invoke
 * {@link #acquire()} before the HTTP call.</p>
 */
@Component
public class PolygonThrottler extends SlidingWindowThrottler {

    public PolygonThrottler(
            @Value("${polygon.ratelimit.max-calls:5}") int maxCallsPerWindow,
            @Value("${polygon.ratelimit.window-ms:62000}") long windowMs) {
        super("Polygon", maxCallsPerWindow, windowMs);
    }
}
