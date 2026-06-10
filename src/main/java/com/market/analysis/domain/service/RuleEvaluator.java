package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

import com.market.analysis.domain.exception.DomainErrorCodes;
import com.market.analysis.domain.exception.RuleNotEvaluableException;
import com.market.analysis.domain.model.EvaluationStatus;
import com.market.analysis.domain.model.IndicatorCode;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleCapability;
import com.market.analysis.domain.model.RuleCapabilityCatalog;
import com.market.analysis.domain.model.RuleResult;
import com.market.analysis.domain.model.Stock;

/**
 * Domain service responsible for evaluating individual technical analysis rules
 * against ticker data.
 *
 * <p>This is a pure domain service with no infrastructure dependencies,
 * ensuring deterministic and testable rule evaluation logic.</p>
 *
 * <p>Since P1 the evaluator is entirely driven by {@link RuleCapabilityCatalog}:
 * indicator resolution is delegated to the capability's {@code IndicatorResolver}
 * instead of being hard-coded in {@code switch} statements.  An unsupported
 * indicator code now throws {@link RuleNotEvaluableException} rather than
 * silently returning {@code null}.</p>
 */
public class RuleEvaluator {

    /**
     * Evaluates a single rule against the provided ticker data.
     *
     * @param rule  the rule to evaluate
     * @param stock the stock data to evaluate against
     * @return RuleResult containing pass/fail status and justification
     * @throws IllegalArgumentException  if rule or stock is null
     * @throws RuleNotEvaluableException if the rule's indicator codes or operator
     *                                   are not supported by the evaluator
     */
    public RuleResult evaluate(Rule rule, Stock stock) {
        Objects.requireNonNull(rule, "Rule cannot be null");
        Objects.requireNonNull(stock, "Stock cannot be null");

        BigDecimal subjectValue = resolveIndicator(rule.getSubjectCode(), rule.getSubjectParam(), stock);
        BigDecimal targetValue = resolveIndicator(rule.getTargetCode(), rule.getTargetParam(), stock);

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
     * Resolves the value of a technical indicator from stock data by delegating
     * to the capability registered in {@link RuleCapabilityCatalog}.
     *
     * @param indicatorCode the code of the indicator (e.g., "PRICE", "SMA", "VOLUME")
     * @param param         optional parameter for the indicator (e.g., 50 for SMA50)
     * @param stock         the stock data
     * @return the indicator value, or {@code null} if the stock has no data for this parameter
     * @throws RuleNotEvaluableException if the indicator code is not in the catalog
     */
    private BigDecimal resolveIndicator(String indicatorCode, Double param, Stock stock) {
        RuleCapability cap = RuleCapabilityCatalog.getCapability(indicatorCode)
                .orElseThrow(() -> new RuleNotEvaluableException(DomainErrorCodes.RULE_NOT_EVALUABLE));
        return cap.resolve(param, stock);
    }

    /**
     * Evaluates the comparison operator between subject and target values.
     *
     * @throws RuleNotEvaluableException if the operator is not supported
     */
    private boolean evaluateOperator(String operator, BigDecimal subject, BigDecimal target) {
        if (operator == null) {
            throw new RuleNotEvaluableException(DomainErrorCodes.RULE_NOT_EVALUABLE);
        }
        if (!RuleCapabilityCatalog.isOperatorSupported(operator)) {
            throw new RuleNotEvaluableException(DomainErrorCodes.RULE_NOT_EVALUABLE);
        }

        return switch (operator.toUpperCase()) {
            case ">", "GREATER_THAN" -> subject.compareTo(target) > 0;
            case ">=", "GREATER_THAN_OR_EQUAL" -> subject.compareTo(target) >= 0;
            case "<", "LESS_THAN" -> subject.compareTo(target) < 0;
            case "<=", "LESS_THAN_OR_EQUAL" -> subject.compareTo(target) <= 0;
            case "=", "==", "EQUALS" -> subject.compareTo(target) == 0;
            case "!=", "NOT_EQUALS" -> subject.compareTo(target) != 0;
            default -> throw new RuleNotEvaluableException(DomainErrorCodes.RULE_NOT_EVALUABLE);
        };
    }

    /**
     * Builds a human-readable justification for the rule evaluation result.
     */
    private String buildJustification(Rule rule, BigDecimal subjectValue, BigDecimal targetValue, boolean passed) {
        String status = passed ? EvaluationStatus.PASSED.getStatus() : EvaluationStatus.FAILED.getStatus();
        return String.format(Locale.ENGLISH, "%s: %s (%.2f) %s %s (%.2f)",
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
            return IndicatorCode.UNKNOWN.getCode();
        }

        if (param == null) {
            return code;
        }

        return switch (code.toUpperCase()) {
            case "SMA" -> String.format("SMA%d", param.intValue());
            case "EMA" -> String.format("EMA%d", param.intValue());
            case "RSI" -> String.format("RSI%d", param.intValue());
            case "CONSTANT" -> String.format(Locale.ENGLISH, "%.2f", param);
            default -> code;
        };
    }
}
