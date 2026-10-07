package com.market.analysis.presentation.util;

import java.time.Duration;
import java.time.Instant;

/**
 * Formats elapsed times for the background-job progress banners
 * ({@code m:ss}, e.g. {@code 2:35}). Single place so every banner
 * renders the same initial text the JavaScript clock then keeps ticking.
 */
public final class ElapsedTimeFormatter {

    private ElapsedTimeFormatter() {
        // utility class – no instantiation
    }

    public static String format(Instant startedAt, Instant now) {
        long seconds = Math.max(0, Duration.between(startedAt, now).getSeconds());
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }
}
