package com.market.analysis.infrastructure.external.shared;

import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import lombok.extern.slf4j.Slf4j;

/**
 * Shared in-memory sliding-window throttler for external market-data APIs.
 *
 * <p>A single bean owns the per-window budget, so every call to the same
 * provider goes through the same bucket. Callers just invoke {@link #acquire()}
 * before the HTTP call. Provider subclasses (Finnhub, Polygon) only bind
 * their name and limits, keeping the algorithm in one place.</p>
 */
@Slf4j
public class SlidingWindowThrottler {

    private static final long SAFETY_MARGIN_MS = 200;

    private final String name;
    private final int maxCallsPerWindow;
    private final long windowMs;
    private final Deque<Instant> apiCallTimestamps = new ConcurrentLinkedDeque<>();

    protected SlidingWindowThrottler(String name, int maxCallsPerWindow, long windowMs) {
        this.name = name;
        this.maxCallsPerWindow = maxCallsPerWindow;
        this.windowMs = windowMs;
    }

    /**
     * Blocks until there is budget for one more call, then records it.
     * Waiting threads release the monitor via {@code wait}, so concurrent
     * Tomcat workers queue fairly instead of piling up on the provider.
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
                    log.debug("{} rate window saturated ({} calls). Waiting {}ms...",
                            name, apiCallTimestamps.size(), waitTime);
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
