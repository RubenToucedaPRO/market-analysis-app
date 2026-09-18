package com.market.analysis.infrastructure.external.polygon;

import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Explicit in-memory throttler for Polygon API calls.
 *
 * <p>Same sliding-window pattern as {@code FinnhubThrottler}: a single shared
 * bean owns the per-minute budget, so every historical-data fetch goes through
 * the same bucket. Callers just invoke {@link #acquire()} before the HTTP call.</p>
 */
@Component
@Slf4j
public class PolygonThrottler {

    private static final long SAFETY_MARGIN_MS = 200;

    private final int maxCallsPerWindow;
    private final long windowMs;
    private final Deque<Instant> apiCallTimestamps = new ConcurrentLinkedDeque<>();

    public PolygonThrottler(
            @Value("${polygon.ratelimit.max-calls:5}") int maxCallsPerWindow,
            @Value("${polygon.ratelimit.window-ms:62000}") long windowMs) {
        this.maxCallsPerWindow = maxCallsPerWindow;
        this.windowMs = windowMs;
    }

    /**
     * Blocks until there is budget for one more call, then records it.
     * Waiting threads release the monitor via {@code wait}, so concurrent
     * Tomcat workers queue fairly instead of piling up on Polygon.
     */
    public synchronized void acquire() {
        removeExpiredTimestamps();
        Instant oldestCall = apiCallTimestamps.peekFirst();

        while (apiCallTimestamps.size() >= maxCallsPerWindow && oldestCall != null) {
            long elapsed = Instant.now().toEpochMilli() - oldestCall.toEpochMilli();
            long waitTime = windowMs - elapsed;

            // Remaining wait = what is left until the oldest call expires,
            // NOT its age. Waiting the age would need several short naps.
            if (waitTime > 0) {
                try {
                    log.debug("Polygon rate window saturated ({} calls). Waiting {}ms...",
                            apiCallTimestamps.size(), waitTime);
                    this.wait(waitTime + SAFETY_MARGIN_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            removeExpiredTimestamps();
            oldestCall = apiCallTimestamps.peekFirst();
        }

        apiCallTimestamps.addLast(Instant.now());
    }

    private void removeExpiredTimestamps() {
        Instant windowStart = Instant.now().minusMillis(windowMs);
        while (!apiCallTimestamps.isEmpty() && apiCallTimestamps.peekFirst().isBefore(windowStart)) {
            apiCallTimestamps.pollFirst();
        }
    }
}
