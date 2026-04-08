package com.market.analysis.domain.model;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Canonical catalog of supported rule capabilities.
 *
 * <p>This is the single source of truth for valid indicator codes,
 * parameter requirements, allowed parameter values and comparison operators.
 * The catalog mirrors exactly what {@code RuleEvaluator} can resolve so that
 * validation at the use-case layer blocks any combination that would cause a
 * silent failure at runtime.</p>
 */
public final class RuleCapabilityCatalog {

    /**
     * Set of comparison operator strings accepted by the rule evaluator.
     * Comparison is performed case-insensitively.
     */
    public static final Set<String> VALID_OPERATORS = Set.of(
            ">", ">=", "<", "<=", "=", "==", "!=",
            "GREATER_THAN", "GREATER_THAN_OR_EQUAL",
            "LESS_THAN", "LESS_THAN_OR_EQUAL",
            "EQUALS", "NOT_EQUALS"
    );

    private static final Map<String, RuleCapability> CAPABILITIES = Map.ofEntries(
            Map.entry("PRICE",      RuleCapability.noParam()),
            Map.entry("SMA",        RuleCapability.withAllowedParams(Set.of(20.0, 50.0, 200.0))),
            Map.entry("EMA",        RuleCapability.withAllowedParams(Set.of(9.0, 12.0, 20.0, 26.0, 50.0, 200.0))),
            Map.entry("RSI",        RuleCapability.withAllowedParams(Set.of(14.0, 30.0))),
            Map.entry("MACD_LINE",  RuleCapability.noParam()),
            Map.entry("MACD_SIGNAL",RuleCapability.noParam()),
            Map.entry("MACD_HIST",  RuleCapability.noParam()),
            Map.entry("BB_UPPER",   RuleCapability.withAllowedParams(Set.of(20.0))),
            Map.entry("BB_LOWER",   RuleCapability.withAllowedParams(Set.of(20.0))),
            Map.entry("ATR",        RuleCapability.withAllowedParams(Set.of(14.0))),
            Map.entry("VOLUME",     RuleCapability.noParam()),
            Map.entry("AVG_VOLUME", RuleCapability.noParam()),
            Map.entry("OPEN",       RuleCapability.noParam()),
            Map.entry("HIGH",       RuleCapability.noParam()),
            Map.entry("LOW",        RuleCapability.noParam()),
            Map.entry("PREV_CLOSE", RuleCapability.noParam()),
            Map.entry("CONSTANT",   RuleCapability.anyParam()),
            Map.entry("VALUE",      RuleCapability.anyParam())
    );

    private RuleCapabilityCatalog() {
    }

    /**
     * Returns whether the given indicator code is supported by the rule evaluator.
     *
     * @param code indicator code (case-insensitive)
     */
    public static boolean isSupported(String code) {
        if (code == null) {
            return false;
        }
        return CAPABILITIES.containsKey(code.toUpperCase());
    }

    /**
     * Returns the capability for the given indicator code, if it exists.
     *
     * @param code indicator code (case-insensitive)
     */
    public static Optional<RuleCapability> getCapability(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(CAPABILITIES.get(code.toUpperCase()));
    }

    /**
     * Returns whether the given operator is supported by the rule evaluator.
     *
     * @param operator comparison operator string
     */
    public static boolean isOperatorSupported(String operator) {
        if (operator == null) {
            return false;
        }
        return VALID_OPERATORS.contains(operator) || VALID_OPERATORS.contains(operator.toUpperCase());
    }

    /**
     * Returns the complete set of supported indicator codes (uppercase).
     */
    public static Set<String> getSupportedCodes() {
        return CAPABILITIES.keySet();
    }
}
