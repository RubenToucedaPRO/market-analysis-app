package com.market.analysis.application.usecase;

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
import com.market.analysis.domain.port.in.EvaluateStrategyUseCase;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
import com.market.analysis.domain.service.RuleEvaluator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Application service implementing strategy evaluation use case.
 * 
 * This service orchestrates the evaluation of trading strategies against
 * ticker data by delegating individual rule evaluations to the RuleEvaluator
 * domain service.
 * 
 * Follows Clean Architecture principles with no infrastructure dependencies.
 */
@RequiredArgsConstructor
@Slf4j
public class EvaluateStrategyService implements EvaluateStrategyUseCase {

    private static final String PASSED = "PASSED";
    private static final String FAILED = "FAILED";

    private final RuleEvaluator ruleEvaluator;
    private final StrategyEvaluationRepository strategyEvaluationRepository;

    @Override
    public AnalysisResult evaluateStrategy(Strategy strategy, Stock stock) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        if (stock == null) {
            throw new IllegalArgumentException("Stock data cannot be null");
        }

        log.info("Evaluating strategy '{}' for ticker '{}'", strategy.getName(), stock.getTicker());

        // Validate strategy consistency
        strategy.validateConsistency();

        // Evaluate each rule in the strategy
        List<RuleResult> ruleResults = new ArrayList<>();
        for (Rule rule : strategy.getRules()) {
            RuleResult result = ruleEvaluator.evaluate(rule, stock);
            ruleResults.add(result);
            log.debug("Rule '{}' evaluation: {}", rule.getName(), result.isPassed() ? PASSED : FAILED);
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

        log.info("Strategy evaluation completed for '{}': {} (Compliance: {}%)",
                stock.getTicker(),
                overallPassed ? PASSED : FAILED,
                result.calculateComplianceRate());

        // Persist evaluation result
        persistStrategyEvaluation(stock, strategy.getId(), result);

        return result;
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

    /**
     * Persists the strategy evaluation result in the database.
     * Marks previous evaluations for this ticker+strategy as non-latest.
     */
    private void persistStrategyEvaluation(Stock stock, Long strategyId, AnalysisResult analysisResult) {
        try {
            StrategyEvaluation evaluation = StrategyEvaluation.builder()
                    .ticker(stock.getTicker())
                    .strategyId(strategyId)
                    .strategyName(analysisResult.getStrategy().getName())
                    .compliant(analysisResult.isOverallPassed())
                    .complianceRate(analysisResult.calculateComplianceRate())
                    .summary(analysisResult.getSummary())
                    .evaluatedAt(analysisResult.getAnalysisTimestamp())
                    .priceAtEvaluation(stock.getCurrentPrice())
                    .isLatest(true)
                    .build();

            strategyEvaluationRepository.save(evaluation, stock);
            log.debug("Strategy evaluation persisted for ticker: {}, strategyId: {}",
                    stock.getTicker(), strategyId);
        } catch (Exception e) {
            log.error("Failed to persist strategy evaluation for ticker: {}, strategyId: {}",
                    stock.getTicker(), strategyId, e);
            // Log error but don't fail the evaluation - persistence is secondary to
            // evaluation
        }
    }
}
