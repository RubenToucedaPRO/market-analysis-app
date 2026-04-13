package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Canonical catalog of supported rule capabilities.
 *
 * <p>This is the single source of truth for valid indicator codes,
 * parameter requirements, allowed parameter values, comparison operators,
 * role constraints (subject / target), and the {@link IndicatorResolver}
 * that extracts the indicator value from a {@link Stock} instance.</p>
 *
 * <p>The catalog mirrors exactly what {@code RuleEvaluator} can resolve so
 * that validation at the use-case layer blocks any combination that would
 * cause a silent failure at runtime (P0), and so that the evaluator can
 * delegate resolution to the catalog rather than using scattered
 * {@code switch} statements (P1).</p>
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
            Map.entry("PRICE",
                    RuleCapability.noParam(
                            (param, s) -> s.getCurrentPrice(),
                            VALID_OPERATORS, true, true)),
            Map.entry("SMA",
                    RuleCapability.withAllowedParams(
                            Set.of(20.0, 50.0, 200.0),
                            RuleCapabilityCatalog::resolveSma,
                            VALID_OPERATORS, true, true)),
            Map.entry("EMA",
                    RuleCapability.withAllowedParams(
                            Set.of(9.0, 12.0, 20.0, 26.0, 50.0, 200.0),
                            RuleCapabilityCatalog::resolveEma,
                            VALID_OPERATORS, true, true)),
            Map.entry("RSI",
                    RuleCapability.withAllowedParams(
                            Set.of(14.0, 30.0),
                            RuleCapabilityCatalog::resolveRsi,
                            VALID_OPERATORS, true, true)),
            Map.entry("MACD_LINE",
                    RuleCapability.noParam(
                            (param, s) -> s.getMacdLine(),
                            VALID_OPERATORS, true, true)),
            Map.entry("MACD_SIGNAL",
                    RuleCapability.noParam(
                            (param, s) -> s.getMacdSignal(),
                            VALID_OPERATORS, true, true)),
            Map.entry("MACD_HIST",
                    RuleCapability.noParam(
                            (param, s) -> s.getMacdHistogram(),
                            VALID_OPERATORS, true, true)),
            Map.entry("BB_UPPER",
                    RuleCapability.withAllowedParams(
                            Set.of(20.0),
                            (param, s) -> param != null && param.intValue() == 20 ? s.getBbUpper20() : null,
                            VALID_OPERATORS, true, true)),
            Map.entry("BB_LOWER",
                    RuleCapability.withAllowedParams(
                            Set.of(20.0),
                            (param, s) -> param != null && param.intValue() == 20 ? s.getBbLower20() : null,
                            VALID_OPERATORS, true, true)),
            Map.entry("ATR",
                    RuleCapability.withAllowedParams(
                            Set.of(14.0),
                            (param, s) -> param != null && param.intValue() == 14 ? s.getAtr14() : null,
                            VALID_OPERATORS, true, true)),
            Map.entry("VOLUME",
                    RuleCapability.noParam(
                            (param, s) -> s.getVolume() != null ? BigDecimal.valueOf(s.getVolume()) : null,
                            VALID_OPERATORS, true, true)),
            Map.entry("AVG_VOLUME",
                    RuleCapability.noParam(
                            (param, s) -> s.getAverageVolume() != null ? BigDecimal.valueOf(s.getAverageVolume()) : null,
                            VALID_OPERATORS, true, true)),
            Map.entry("OPEN",
                    RuleCapability.noParam(
                            (param, s) -> s.getOpenPrice(),
                            VALID_OPERATORS, true, true)),
            Map.entry("HIGH",
                    RuleCapability.noParam(
                            (param, s) -> s.getHighOfDay(),
                            VALID_OPERATORS, true, true)),
            Map.entry("LOW",
                    RuleCapability.noParam(
                            (param, s) -> s.getLowOfDay(),
                            VALID_OPERATORS, true, true)),
            Map.entry("PREV_CLOSE",
                    RuleCapability.noParam(
                            (param, s) -> s.getPreviousClose(),
                            VALID_OPERATORS, true, true)),
            Map.entry("CONSTANT",
                    RuleCapability.anyParam(
                            (param, s) -> param != null ? BigDecimal.valueOf(param) : null,
                            VALID_OPERATORS, true, true)),
            Map.entry("VALUE",
                    RuleCapability.anyParam(
                            (param, s) -> param != null ? BigDecimal.valueOf(param) : null,
                            VALID_OPERATORS, true, true))
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
     * Returns whether the given indicator code may be used as a rule subject.
     *
     * @param code indicator code (case-insensitive)
     */
    public static boolean isSubjectAllowed(String code) {
        return getCapability(code).map(RuleCapability::isSubjectAllowed).orElse(false);
    }

    /**
     * Returns whether the given indicator code may be used as a rule target.
     *
     * @param code indicator code (case-insensitive)
     */
    public static boolean isTargetAllowed(String code) {
        return getCapability(code).map(RuleCapability::isTargetAllowed).orElse(false);
    }

    /**
     * Returns the complete set of supported indicator codes (uppercase).
     */
    public static Set<String> getSupportedCodes() {
        return CAPABILITIES.keySet();
    }

    // -------------------------------------------------------------------------
    // Private resolver helpers
    // -------------------------------------------------------------------------

    private static BigDecimal resolveSma(Double param, Stock stock) {
        if (param == null) {
            return null;
        }
        return switch (param.intValue()) {
            case 20 -> stock.getSma20();
            case 50 -> stock.getSma50();
            case 200 -> stock.getSma200();
            default -> null;
        };
    }

    private static BigDecimal resolveEma(Double param, Stock stock) {
        if (param == null) {
            return null;
        }
        return switch (param.intValue()) {
            case 9 -> stock.getEma9();
            case 12 -> stock.getEma12();
            case 20 -> stock.getEma20();
            case 26 -> stock.getEma26();
            case 50 -> stock.getEma50();
            case 200 -> stock.getEma200();
            default -> null;
        };
    }

    private static BigDecimal resolveRsi(Double param, Stock stock) {
        if (param == null) {
            return null;
        }
        return switch (param.intValue()) {
            case 14 -> stock.getRsi14();
            case 30 -> stock.getRsi30();
            default -> null;
        };
    }
}
