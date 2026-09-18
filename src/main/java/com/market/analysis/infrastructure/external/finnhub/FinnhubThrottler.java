package com.market.analysis.infrastructure.external.finnhub;

import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Explicit in-memory throttler for Finnhub API calls.
 *
 * <p>Same sliding-window pattern as
 * {@code PolygonAdapter} (per-minute bucket with unconditional wait): both
 * {@code getQuote} and {@code getCompanyProfile} share this single bean, so
 * the configured budget covers every Finnhub call. Replaces the Resilience4j
 * {@code @RateLimiter} annotation, which stayed silent in Docker because its
 * configuration never reached the packaged jar.</p>
 */
@Component
@Slf4j
public class FinnhubThrottler {

    private static final long SAFETY_MARGIN_MS = 200;

    private final int maxCallsPerWindow;
    private final long windowMs;
    private final Deque<Instant> apiCallTimestamps = new ConcurrentLinkedDeque<>();

    public FinnhubThrottler(
            @Value("${finnhub.ratelimit.max-calls:55}") int maxCallsPerWindow,
            @Value("${finnhub.ratelimit.window-ms:65000}") long windowMs) {
        this.maxCallsPerWindow = maxCallsPerWindow;
        this.windowMs = windowMs;
    }

    /**
     * Blocks until there is budget for one more call, then records it.
     * Waiting threads release the monitor via {@code wait}, so concurrent
     * Tomcat workers queue fairly instead of piling up on Finnhub.
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
                    log.debug("Finnhub rate window saturated ({} calls). Waiting {}ms...",
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
