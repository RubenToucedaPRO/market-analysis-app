package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.util.Objects;

import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleResult;
import com.market.analysis.domain.model.Stock;

/**
 * Domain service responsible for evaluating individual technical analysis rules
 * against ticker data.
 * 
 * This is a pure domain service with no infrastructure dependencies,
 * ensuring deterministic and testable rule evaluation logic.
 */
public class RuleEvaluator {

    /**
     * Evaluates a single rule against the provided ticker data.
     * 
     * @param rule the rule to evaluate
     * @param stock the stock data to evaluate against
     * @return RuleResult containing pass/fail status and justification
     * @throws IllegalArgumentException if rule or stock is null
     */
    public RuleResult evaluate(Rule rule, Stock stock) {
        Objects.requireNonNull(rule, "Rule cannot be null");
        Objects.requireNonNull(stock, "Stock cannot be null");

        BigDecimal subjectValue = getIndicatorValue(rule.getSubjectCode(), rule.getSubjectParam(), stock);
        BigDecimal targetValue = getIndicatorValue(rule.getTargetCode(), rule.getTargetParam(), stock);

        if (subjectValue == null || targetValue == null) {
            return RuleResult.builder()
                    .rule(rule)
                    .passed(false)
                    .justification(buildMissingDataJustification(rule, subjectValue, targetValue))
                    .build();
        }

        boolean passed = evaluateOperator(rule.getOperator(), subjectValue, targetValue);
        String justification = buildJustification(rule, subjectValue, targetValue, passed);

        return RuleResult.builder()
                .rule(rule)
                .passed(passed)
                .justification(justification)
                .build();
    }

    /**
     * Gets the value of a technical indicator from stock data.
     * 
     * @param indicatorCode the code of the indicator (e.g., "PRICE", "SMA", "VOLUME")
     * @param param optional parameter for the indicator (e.g., 50 for SMA50)
     * @param stock the stock data
     * @return the indicator value, or null if not available
     */
    private BigDecimal getIndicatorValue(String indicatorCode, Double param, Stock stock) {
        if (indicatorCode == null) {
            return null;
        }

        return switch (indicatorCode.toUpperCase()) {
            case "PRICE" -> stock.getCurrentPrice();
            case "SMA" -> getSmaValue(param, stock);
            case "VOLUME" -> stock.getVolume() != null ? BigDecimal.valueOf(stock.getVolume()) : null;
            case "AVG_VOLUME" -> stock.getAverageVolume() != null ? BigDecimal.valueOf(stock.getAverageVolume()) : null;
            case "CONSTANT" -> param != null ? BigDecimal.valueOf(param) : null;
            case "OPEN" -> stock.getOpenPrice();
            case "HIGH" -> stock.getHighOfDay();
            case "LOW" -> stock.getLowOfDay();
            case "PREV_CLOSE" -> stock.getPreviousClose();
            default -> null;
        };
    }

    /**
     * Gets the SMA value based on the period parameter.
     */
    private BigDecimal getSmaValue(Double param, Stock stock) {
        if (param == null) {
            return null;
        }

        int period = param.intValue();
        return switch (period) {
            case 20 -> stock.getSma20();
            case 50 -> stock.getSma50();
            case 200 -> stock.getSma200();
            default -> null;
        };
    }

    /**
     * Evaluates the comparison operator between subject and target values.
     */
    private boolean evaluateOperator(String operator, BigDecimal subject, BigDecimal target) {
        if (operator == null) {
            return false;
        }

        return switch (operator.toUpperCase()) {
            case ">", "GREATER_THAN" -> subject.compareTo(target) > 0;
            case ">=", "GREATER_THAN_OR_EQUAL" -> subject.compareTo(target) >= 0;
            case "<", "LESS_THAN" -> subject.compareTo(target) < 0;
            case "<=", "LESS_THAN_OR_EQUAL" -> subject.compareTo(target) <= 0;
            case "=", "==", "EQUALS" -> subject.compareTo(target) == 0;
            case "!=", "NOT_EQUALS" -> subject.compareTo(target) != 0;
            default -> false;
        };
    }

    /**
     * Builds a human-readable justification for the rule evaluation result.
     */
    private String buildJustification(Rule rule, BigDecimal subjectValue, BigDecimal targetValue, boolean passed) {
        String status = passed ? "PASSED" : "FAILED";
        return String.format("%s: %s (%.2f) %s %s (%.2f)",
                status,
                formatIndicatorName(rule.getSubjectCode(), rule.getSubjectParam()),
                subjectValue,
                rule.getOperator(),
                formatIndicatorName(rule.getTargetCode(), rule.getTargetParam()),
                targetValue);
    }

    /**
     * Builds justification when required data is missing.
     */
    private String buildMissingDataJustification(Rule rule, BigDecimal subjectValue, BigDecimal targetValue) {
        if (subjectValue == null && targetValue == null) {
            return "FAILED: Missing both subject and target data";
        } else if (subjectValue == null) {
            return String.format("FAILED: Missing subject data for %s",
                    formatIndicatorName(rule.getSubjectCode(), rule.getSubjectParam()));
        } else {
            return String.format("FAILED: Missing target data for %s",
                    formatIndicatorName(rule.getTargetCode(), rule.getTargetParam()));
        }
    }

    /**
     * Formats an indicator name for display.
     */
    private String formatIndicatorName(String code, Double param) {
        if (code == null) {
            return "UNKNOWN";
        }

        if (param == null) {
            return code;
        }

        return switch (code.toUpperCase()) {
            case "SMA" -> String.format("SMA%d", param.intValue());
            case "CONSTANT" -> String.format("%.2f", param);
            default -> code;
        };
    }
}
