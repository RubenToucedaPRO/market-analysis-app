package com.market.analysis.domain.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.market.analysis.domain.model.FinvizFilterMappingResult;
import com.market.analysis.domain.model.Rule;

/**
 * Pure domain mapper that translates internal deterministic rules to Finviz
 * filter codes when an equivalent filter exists.
 */
public class FinvizFilterMapper {

    private static final Map<String, String> OPERATOR_ALIASES = Map.of(
            ">", ">",
            "<", "<",
            "GREATER_THAN", ">",
            "LESS_THAN", "<");

    private static final Map<RulePattern, String> MAPPINGS = Map.ofEntries(
            Map.entry(RulePattern.of("PRICE", null, ">", "SMA", 20.0), "ta_sma20_pa"),
            Map.entry(RulePattern.of("PRICE", null, "<", "SMA", 20.0), "ta_sma20_pb"),
            Map.entry(RulePattern.of("PRICE", null, ">", "SMA", 50.0), "ta_sma50_pa"),
            Map.entry(RulePattern.of("PRICE", null, "<", "SMA", 50.0), "ta_sma50_pb"),
            Map.entry(RulePattern.of("PRICE", null, ">", "SMA", 200.0), "ta_sma200_pa"),
            Map.entry(RulePattern.of("PRICE", null, "<", "SMA", 200.0), "ta_sma200_pb"),
            Map.entry(RulePattern.of("SMA", 20.0, ">", "SMA", 50.0), "ta_sma20_sa50"),
            Map.entry(RulePattern.of("SMA", 20.0, "<", "SMA", 50.0), "ta_sma20_sb50"),
            Map.entry(RulePattern.of("SMA", 50.0, ">", "SMA", 200.0), "ta_sma50_sa200"),
            Map.entry(RulePattern.of("SMA", 50.0, "<", "SMA", 200.0), "ta_sma50_sb200"),
            Map.entry(RulePattern.of("VOLUME", null, ">", "AVG_VOLUME", null), "sh_relvol_o1"),
            Map.entry(RulePattern.of("VOLUME", null, "<", "AVG_VOLUME", null), "sh_relvol_u1"));

    public FinvizFilterMappingResult map(List<Rule> rules) {
        if (rules == null || rules.isEmpty()) {
            return FinvizFilterMappingResult.builder()
                    .filters("")
                    .unmappableRules(List.of())
                    .warnings(List.of())
                    .build();
        }

        Set<String> filters = new LinkedHashSet<>();
        List<String> unmappableRules = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (Rule rule : rules) {
            if (rule == null) {
                unmappableRules.add("NULL_RULE");
                warnings.add("Rule 'NULL_RULE' cannot be mapped to Finviz filters.");
                continue;
            }

            String mappedFilter = MAPPINGS.get(toPattern(rule));
            if (mappedFilter == null) {
                String ruleDescriptor = describe(rule);
                unmappableRules.add(ruleDescriptor);
                warnings.add("Rule '" + ruleDescriptor + "' cannot be mapped to Finviz filters.");
                continue;
            }

            filters.add(mappedFilter);
        }

        return FinvizFilterMappingResult.builder()
                .filters(String.join(",", filters))
                .unmappableRules(unmappableRules)
                .warnings(warnings)
                .build();
    }

    private RulePattern toPattern(Rule rule) {
        return RulePattern.of(
                normalizeCode(rule.getSubjectCode()),
                normalizeParam(rule.getSubjectParam()),
                normalizeOperator(rule.getOperator()),
                normalizeCode(rule.getTargetCode()),
                normalizeParam(rule.getTargetParam()));
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.toUpperCase(Locale.ROOT).trim();
    }

    private Double normalizeParam(Double param) {
        if (param == null) {
            return null;
        }
        if (Math.floor(param) == param) {
            return Double.valueOf(param.intValue());
        }
        return param;
    }

    private String normalizeOperator(String operator) {
        if (operator == null) {
            return null;
        }
        String normalized = operator.toUpperCase(Locale.ROOT).trim();
        return OPERATOR_ALIASES.getOrDefault(normalized, normalized);
    }

    private String describe(Rule rule) {
        return formatIndicator(rule.getSubjectCode(), rule.getSubjectParam())
                + " " + rule.getOperator() + " "
                + formatIndicator(rule.getTargetCode(), rule.getTargetParam());
    }

    private String formatIndicator(String code, Double param) {
        if (code == null) {
            return "UNKNOWN";
        }
        if (param == null) {
            return code.toUpperCase(Locale.ROOT).trim();
        }
        return code.toUpperCase(Locale.ROOT).trim() + "(" + formatParam(param) + ")";
    }

    private String formatParam(Double param) {
        return Math.floor(param) == param ? Integer.toString(param.intValue()) : param.toString();
    }

    private record RulePattern(String subjectCode, Double subjectParam, String operator, String targetCode, Double targetParam) {
        private static RulePattern of(String subjectCode, Double subjectParam, String operator, String targetCode,
                Double targetParam) {
            return new RulePattern(subjectCode, subjectParam, operator, targetCode, targetParam);
        }
    }
}
