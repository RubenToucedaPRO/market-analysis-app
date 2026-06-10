package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.market.analysis.domain.exception.DomainErrorCodes;
import com.market.analysis.domain.exception.DomainValidationException;
import com.market.analysis.domain.exception.MissingIndicatorException;
import com.market.analysis.domain.model.AnalysisResult;
import com.market.analysis.domain.model.EvaluationStatus;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleResult;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyEvaluation;

/**
 * Application service implementing strategy evaluation use case.
 * 
 * This service orchestrates the evaluation of trading strategies against
 * ticker data by delegating individual rule evaluations to the RuleEvaluator
 * domain service.
 * 
 * Follows Clean Architecture principles with no infrastructure dependencies.
 */

public class EvaluateStrategyService {

    private static final String METRIC_TOTAL_RULES = "totalRules";
    private static final String METRIC_PASSED_RULES = "passedRules";
    private static final String METRIC_FAILED_RULES = "failedRules";

    private static final String SUMMARY_TEMPLATE = "Strategy '%s' evaluation for %s: %s. ";
    private static final String RULES_PASSED_TEMPLATE = "%d/%d rules passed.";
    private static final String SUFFIX_FAILED_RULES = " Failed rules: ";
    private static final String MSG_RISK_PLAN_FAILED = " Risk plan could not be calculated: ";

    private final RuleEvaluator ruleEvaluator;
    private final RiskRewardCalculator riskRewardCalculator;

    public EvaluateStrategyService(RuleEvaluator ruleEvaluator, RiskRewardCalculator riskRewardCalculator) {
        this.ruleEvaluator = ruleEvaluator;
        this.riskRewardCalculator = riskRewardCalculator;
    }

    public StrategyEvaluation evaluateStrategy(Strategy strategy, Stock stock) {
        if (strategy == null) {
            throw new DomainValidationException(DomainErrorCodes.STRATEGY_NULL);
        }
        if (stock == null) {
            throw new DomainValidationException(DomainErrorCodes.STOCK_DATA_NULL);
        }

        strategy.validateConsistency();

        List<RuleResult> ruleResults = new ArrayList<>();
        for (Rule rule : strategy.getRules()) {
            RuleResult result = ruleEvaluator.evaluate(rule, stock);
            ruleResults.add(result);
        }

        Map<String, Object> metrics = calculateMetrics(ruleResults);
        boolean overallPassed = determineOverallResult(ruleResults);
        String summary = generateSummary(strategy, stock.getTicker(), ruleResults, overallPassed);

        AnalysisResult result = AnalysisResult.builder()
                .strategy(strategy)
                .ticker(stock.getTicker())
                .analysisTimestamp(Instant.now())
                .ruleResults(ruleResults)
                .calculatedMetrics(metrics)
                .overallPassed(overallPassed)
                .summary(summary)
                .build();

        BigDecimal targetPrice = null;
        BigDecimal stopLossPrice = null;
        BigDecimal riskRewardRatio = null;
        Integer recommendedShares = null;

        if (overallPassed) {
            try {
                BigDecimal entryPrice = stock.getCurrentPrice();
                targetPrice = riskRewardCalculator.calculateTargetPrice(entryPrice, strategy.getObjective(), stock);
                stopLossPrice = riskRewardCalculator.calculateStopLossPrice(entryPrice, strategy.getObjective(), stock);
                riskRewardRatio = riskRewardCalculator.calculateRiskRewardRatio(entryPrice, targetPrice, stopLossPrice);
                recommendedShares = riskRewardCalculator
                        .calculatePositionSize(entryPrice, stopLossPrice, strategy.getObjective().getCapitalToRisk())
                        .intValue();
            } catch (MissingIndicatorException | IllegalArgumentException e) {
                targetPrice = null;
                stopLossPrice = null;
                riskRewardRatio = null;
                recommendedShares = null;
                summary = summary + MSG_RISK_PLAN_FAILED + e.getMessage();
            }
        }

        return StrategyEvaluation.builder()
                .ticker(stock.getTicker())
                .strategyId(strategy.getId())
                .strategyName(result.getStrategy().getName())
                .compliant(result.isOverallPassed())
                .complianceRate(result.calculateComplianceRate())
                .summary(summary)
                .evaluatedAt(result.getAnalysisTimestamp())
                .priceAtEvaluation(stock.getCurrentPrice())
                .isLatest(true)
                .targetPrice(targetPrice)
                .stopLossPrice(stopLossPrice)
                .riskRewardRatio(riskRewardRatio)
                .recommendedShares(recommendedShares)
                .build();
    }

    /**
     * Calculates metrics from rule evaluation results.
     */
    private Map<String, Object> calculateMetrics(List<RuleResult> ruleResults) {
        Map<String, Object> metrics = new HashMap<>();

        long passedCount = ruleResults.stream().filter(RuleResult::isPassed).count();
        long totalCount = ruleResults.size();

        metrics.put(METRIC_TOTAL_RULES, totalCount);
        metrics.put(METRIC_PASSED_RULES, passedCount);
        metrics.put(METRIC_FAILED_RULES, totalCount - passedCount);

        return metrics;
    }

    /**
     * Determines the overall pass/fail status of the strategy evaluation.
     * Currently requires ALL rules to pass (AND logic).
     */
    private boolean determineOverallResult(List<RuleResult> ruleResults) {
        return ruleResults.stream().allMatch(RuleResult::isPassed);
    }

    /**
     * Generates a human-readable summary of the evaluation.
     */
    private String generateSummary(Strategy strategy, String ticker, List<RuleResult> ruleResults,
            boolean overallPassed) {
        long passedCount = ruleResults.stream().filter(RuleResult::isPassed).count();
        long totalCount = ruleResults.size();

        StringBuilder summary = new StringBuilder();
        summary.append(String.format(SUMMARY_TEMPLATE,
                strategy.getName(),
                ticker,
                overallPassed ? EvaluationStatus.PASSED.getStatus() : EvaluationStatus.FAILED.getStatus()));
        summary.append(String.format(RULES_PASSED_TEMPLATE, passedCount, totalCount));

        if (!overallPassed) {
            summary.append(SUFFIX_FAILED_RULES);
            List<String> failedRules = ruleResults.stream()
                    .filter(r -> !r.isPassed())
                    .map(r -> r.getRule().getName())
                    .toList();
            summary.append(String.join(", ", failedRules));
        }

        return summary.toString();
    }

}
