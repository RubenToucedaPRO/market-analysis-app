package com.market.analysis.domain.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Canonical enumeration of all supported technical indicator codes.
 *
 * <p>This enum is the single source of truth for valid indicator codes
 * used across the domain layer. It replaces scattered string literals
 * and provides type-safe references with O(1) lookup via {@link #fromCode(String)}.</p>
 */
public enum IndicatorCode {

    PRICE("PRICE"),
    SMA("SMA"),
    EMA("EMA"),
    RSI("RSI"),
    MACD_LINE("MACD_LINE"),
    MACD_SIGNAL("MACD_SIGNAL"),
    MACD_HIST("MACD_HIST"),
    BB_UPPER("BB_UPPER"),
    BB_LOWER("BB_LOWER"),
    ATR("ATR"),
    VOLUME("VOLUME"),
    AVG_VOLUME("AVG_VOLUME"),
    OPEN("OPEN"),
    HIGH("HIGH"),
    LOW("LOW"),
    PREV_CLOSE("PREV_CLOSE"),
    CONSTANT("CONSTANT"),
    VALUE("VALUE"),
    UNKNOWN("UNKNOWN");

    private final String code;

    private static final Map<String, IndicatorCode> BY_CODE =
            Arrays.stream(values())
                    .collect(Collectors.toMap(IndicatorCode::getCode, Function.identity()));

    IndicatorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Returns the {@link IndicatorCode} for the given code string.
     *
     * @param code the indicator code (case-sensitive)
     * @return the matching enum constant
     * @throws IllegalArgumentException if no constant matches the code
     */
    public static IndicatorCode fromCode(String code) {
        IndicatorCode result = BY_CODE.get(code);
        if (result == null) {
            throw new IllegalArgumentException("Unknown indicator code: " + code);
        }
        return result;
    }
}
