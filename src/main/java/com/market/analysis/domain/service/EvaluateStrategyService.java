package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.market.analysis.domain.exception.DomainErrorCodes;
import com.market.analysis.domain.exception.DomainValidationException;
import com.market.analysis.domain.exception.MissingIndicatorException;
import com.market.analysis.domain.model.AnalysisResult;
import com.market.analysis.domain.model.EntryPrice;
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
    private static final String METRIC_SCORE = "score";
    private static final String METRIC_PASSED_WEIGHT = "passedWeight";
    private static final String METRIC_TOTAL_WEIGHT = "totalWeight";

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
        BigDecimal score = (BigDecimal) metrics.get(METRIC_SCORE);
        boolean overallPassed = determineOverallResult(strategy, score);
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
                EntryPrice entryPrice = EntryPrice.of(stock.getCurrentPrice());
                targetPrice = riskRewardCalculator.calculateTargetPrice(entryPrice, strategy.getObjective(), stock);
                stopLossPrice = riskRewardCalculator.calculateStopLossPrice(entryPrice, strategy.getObjective(), stock);
                riskRewardRatio = riskRewardCalculator.calculateRiskRewardRatio(entryPrice, targetPrice, stopLossPrice);
                recommendedShares = riskRewardCalculator
                        .calculatePositionSize(entryPrice, stopLossPrice, strategy.getObjective().getCapitalToRisk())
                        .intValue();
            } catch (MissingIndicatorException | DomainValidationException | IllegalArgumentException e) {
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
     * Includes the weight-weighted score (0-100): the percentage of total
     * rule weight contributed by passed rules.
     */
    private Map<String, Object> calculateMetrics(List<RuleResult> ruleResults) {
        Map<String, Object> metrics = new HashMap<>();

        long passedCount = ruleResults.stream().filter(RuleResult::isPassed).count();
        long totalCount = ruleResults.size();

        metrics.put(METRIC_TOTAL_RULES, totalCount);
        metrics.put(METRIC_PASSED_RULES, passedCount);
        metrics.put(METRIC_FAILED_RULES, totalCount - passedCount);

        long totalWeight = 0;
        long passedWeight = 0;
        for (RuleResult ruleResult : ruleResults) {
            int weight = ruleResult.getRule().getWeight();
            totalWeight += weight;
            if (ruleResult.isPassed()) {
                passedWeight += weight;
            }
        }

        BigDecimal score = totalWeight == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(passedWeight)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP);

        metrics.put(METRIC_SCORE, score);
        metrics.put(METRIC_PASSED_WEIGHT, passedWeight);
        metrics.put(METRIC_TOTAL_WEIGHT, totalWeight);

        return metrics;
    }

    /**
     * Determines the overall pass/fail status of the strategy evaluation.
     * The strategy passes when its weighted score meets the strategy threshold.
     * The default threshold of 100 preserves the previous AND logic
     * (all rules must pass).
     */
    private boolean determineOverallResult(Strategy strategy, BigDecimal score) {
        return score.compareTo(BigDecimal.valueOf(strategy.getThreshold())) >= 0;
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
