package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.market.analysis.domain.model.AnalysisResult;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleResult;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.model.StrategyObjective;

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

    private static final String PASSED = "PASSED";
    private static final String FAILED = "FAILED";

    private final RuleEvaluator ruleEvaluator;

    public EvaluateStrategyService(RuleEvaluator ruleEvaluator) {
        this.ruleEvaluator = ruleEvaluator;
    }

    public StrategyEvaluation evaluateStrategy(Strategy strategy, Stock stock) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        if (stock == null) {
            throw new IllegalArgumentException("Stock data cannot be null");
        }

        // Validate strategy consistency
        strategy.validateConsistency();

        // Evaluate each rule in the strategy
        List<RuleResult> ruleResults = new ArrayList<>();
        for (Rule rule : strategy.getRules()) {
            RuleResult result = ruleEvaluator.evaluate(rule, stock);
            ruleResults.add(result);
        }

        // Calculate metrics
        Map<String, Object> metrics = calculateMetrics(ruleResults);

        // Calculate Risk:Reward if objective is defined
        BigDecimal riskRewardRatio = null;
        BigDecimal rewardPercentage = null;
        BigDecimal riskPercentage = null;
        Integer shareQuantity = null;

        if (strategy.hasObjective()) {
            StrategyObjective objective = strategy.getObjective();
            BigDecimal entryPrice = stock.getCurrentPrice();

            try {
                riskRewardRatio = objective.calculateRiskRewardRatio(entryPrice, stock);
                rewardPercentage = objective.calculateRewardPercentage(entryPrice, stock);
                riskPercentage = objective.calculateRiskPercentage(entryPrice, stock);
                shareQuantity = objective.calculateShareQuantity(entryPrice, stock);

                // Add R:R metrics to calculated metrics map
                metrics.put("riskRewardRatio", riskRewardRatio);
                metrics.put("rewardPercentage", rewardPercentage);
                metrics.put("riskPercentage", riskPercentage);
                if (shareQuantity != null) {
                    metrics.put("shareQuantity", shareQuantity);
                }
            } catch (IllegalStateException | IllegalArgumentException e) {
                // If objective validation fails, log and continue without R:R
                // This allows strategies with invalid objectives to still be evaluated
                metrics.put("riskRewardError", e.getMessage());
            }
        }

        // Determine overall pass/fail
        boolean overallPassed = determineOverallResult(ruleResults);

        // Generate summary
        String summary = generateSummary(strategy, stock.getTicker(), ruleResults, overallPassed, riskRewardRatio);

        AnalysisResult result = AnalysisResult.builder()
                .strategy(strategy)
                .ticker(stock.getTicker())
                .analysisTimestamp(Instant.now())
                .ruleResults(ruleResults)
                .calculatedMetrics(metrics)
                .overallPassed(overallPassed)
                .summary(summary)
                .build();

        return StrategyEvaluation.builder()
                .ticker(stock.getTicker())
                .strategyId(strategy.getId())
                .strategyName(result.getStrategy().getName())
                .compliant(result.isOverallPassed())
                .complianceRate(result.calculateComplianceRate())
                .summary(result.getSummary())
                .evaluatedAt(result.getAnalysisTimestamp())
                .priceAtEvaluation(stock.getCurrentPrice())
                .isLatest(true)
                .riskRewardRatio(riskRewardRatio)
                .rewardPercentage(rewardPercentage)
                .riskPercentage(riskPercentage)
                .shareQuantity(shareQuantity)
                .build();
    }

    /**
     * Calculates metrics from rule evaluation results.
     */
    private Map<String, Object> calculateMetrics(List<RuleResult> ruleResults) {
        Map<String, Object> metrics = new HashMap<>();

        long passedCount = ruleResults.stream().filter(RuleResult::isPassed).count();
        long totalCount = ruleResults.size();

        metrics.put("totalRules", totalCount);
        metrics.put("passedRules", passedCount);
        metrics.put("failedRules", totalCount - passedCount);

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
            boolean overallPassed, BigDecimal riskRewardRatio) {
        long passedCount = ruleResults.stream().filter(RuleResult::isPassed).count();
        long totalCount = ruleResults.size();

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Strategy '%s' evaluation for %s: %s. ",
                strategy.getName(),
                ticker,
                overallPassed ? PASSED : FAILED));
        summary.append(String.format("%d/%d rules passed.", passedCount, totalCount));

        // Add R:R information if available
        if (riskRewardRatio != null) {
            summary.append(String.format(" Risk:Reward ratio: 1:%.2f.", riskRewardRatio.doubleValue()));
        }

        if (!overallPassed) {
            summary.append(" Failed rules: ");
            List<String> failedRules = ruleResults.stream()
                    .filter(r -> !r.isPassed())
                    .map(r -> r.getRule().getName())
                    .toList();
            summary.append(String.join(", ", failedRules));
        }

        return summary.toString();
    }

}
