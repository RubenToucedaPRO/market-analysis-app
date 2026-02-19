package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.market.analysis.domain.exception.MissingIndicatorException;
import com.market.analysis.domain.model.AnalysisResult;
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

    private static final String PASSED = "PASSED";
    private static final String FAILED = "FAILED";

    private final RuleEvaluator ruleEvaluator;
    private final RiskRewardCalculator riskRewardCalculator;

    public EvaluateStrategyService(RuleEvaluator ruleEvaluator, RiskRewardCalculator riskRewardCalculator) {
        this.ruleEvaluator = ruleEvaluator;
        this.riskRewardCalculator = riskRewardCalculator;
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

        // Determine overall pass/fail
        boolean overallPassed = determineOverallResult(ruleResults);

        // Generate summary
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
            } catch (MissingIndicatorException e) {
                summary = summary + " Risk plan could not be calculated: " + e.getMessage();
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
            boolean overallPassed) {
        long passedCount = ruleResults.stream().filter(RuleResult::isPassed).count();
        long totalCount = ruleResults.size();

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Strategy '%s' evaluation for %s: %s. ",
                strategy.getName(),
                ticker,
                overallPassed ? PASSED : FAILED));
        summary.append(String.format("%d/%d rules passed.", passedCount, totalCount));

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
