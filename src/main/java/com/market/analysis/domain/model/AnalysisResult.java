package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Entity representing the result of evaluating a strategy against ticker data.
 * Contains the strategy, ticker information, individual rule results, and calculated metrics.
 */
@Getter
@Builder
@ToString
public class AnalysisResult {

    /**
     * The strategy that was evaluated.
     */
    private final Strategy strategy;

    /**
     * The ticker that was analyzed.
     */
    private final String ticker;

    /**
     * Timestamp when the analysis was performed.
     */
    private final LocalDateTime analysisTimestamp;

    /**
     * Results of individual rule evaluations.
     */
    private final List<RuleResult> ruleResults;

    /**
     * Calculated metrics from the analysis.
     * Can include risk/reward ratio, compliance rate, confidence score, etc.
     */
    private final Map<String, Object> calculatedMetrics;

    /**
     * Overall result indicating if the strategy criteria are met.
     */
    private final boolean overallPassed;

    /**
     * Summary of the analysis.
     */
    private final String summary;

    /**
     * Gets an immutable copy of the rule results list.
     *
     * @return unmodifiable list of rule results
     */
    public List<RuleResult> getRuleResults() {
        return ruleResults != null ? List.copyOf(ruleResults) : List.of();
    }

    /**
     * Validates the consistency of the analysis result.
     * Ensures all required fields are present and valid.
     *
     * @throws IllegalStateException if the analysis result is not properly configured
     */
    public void validateConsistency() {
        if (strategy == null) {
            throw new IllegalStateException("Strategy cannot be null");
        }
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalStateException("Ticker cannot be null or empty");
        }
        if (analysisTimestamp == null) {
            throw new IllegalStateException("Analysis timestamp cannot be null");
        }
        if (ruleResults == null) {
            throw new IllegalStateException("Rule results cannot be null");
        }
        if (calculatedMetrics == null) {
            throw new IllegalStateException("Calculated metrics cannot be null");
        }

        // Validate that number of rule results matches number of rules in strategy
        if (ruleResults.size() != strategy.getRules().size()) {
            throw new IllegalStateException(
                String.format("Number of rule results (%d) does not match number of rules in strategy (%d)",
                    ruleResults.size(), strategy.getRules().size())
            );
        }
    }

    /**
     * Calculates the percentage of rules that passed.
     *
     * @return compliance rate as a percentage (0-100)
     */
    public BigDecimal calculateComplianceRate() {
        if (ruleResults.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long passedCount = ruleResults.stream()
                .filter(RuleResult::isPassed)
                .count();

        return BigDecimal.valueOf(passedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(ruleResults.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Custom builder to ensure defensive copying of collections.
     */
    public static class AnalysisResultBuilder {
        public AnalysisResultBuilder ruleResults(List<RuleResult> ruleResults) {
            this.ruleResults = ruleResults != null ? new ArrayList<>(ruleResults) : new ArrayList<>();
            return this;
        }
    }
}
