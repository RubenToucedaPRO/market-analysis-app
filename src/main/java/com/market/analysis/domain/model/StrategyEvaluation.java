package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents the persisted result of a strategy evaluation for a ticker.
 * Immutable historical record of evaluation at a specific point in time.
 * 
 * This domain model separates the transient evaluation result (AnalysisResult)
 * from the persistent record, following Clean Architecture principles.
 */
@Getter
@Builder(toBuilder = true)
@ToString
public class StrategyEvaluation {

    /**
     * Unique identifier for this evaluation record.
     */
    private Long id;

    /**
     * Reference to the ticker that was evaluated.
     */
    private String ticker;

    /**
     * Reference to the strategy that was evaluated.
     */
    private String strategyName;

    /**
     * Reference to the strategy that was evaluated.
     */
    private Long strategyId;

    /**
     * Whether the strategy evaluation passed or failed.
     */
    private boolean compliant;

    /**
     * Compliance rate as a percentage (0-100).
     * Calculated from the number of rules that passed.
     */
    private BigDecimal complianceRate;

    /**
     * Human-readable summary of the analysis.
     * Can include interpretation from IA if available.
     */
    private String summary;

    /**
     * Timestamp when the evaluation was performed.
     * Allows tracking staleness of evaluations.
     */
    private Instant evaluatedAt;

    /**
     * Snapshot of the stock price at evaluation time.
     * Used for historical reference and audit trail.
     */
    private BigDecimal priceAtEvaluation;

    /**
     * Indicates if this is the latest evaluation for this ticker+strategy combo.
     * Helps optimize queries for most recent evaluation.
     */
    private boolean isLatest;

    /**
     * Calculated target price for the trade plan at evaluation time.
     * Null if the strategy was not compliant or if a required indicator was missing.
     */
    private BigDecimal targetPrice;

    /**
     * Calculated stop-loss price for the trade plan at evaluation time.
     * Null if the strategy was not compliant or if a required indicator was missing.
     */
    private BigDecimal stopLossPrice;

    /**
     * Risk-reward ratio calculated at evaluation time.
     * Null if the strategy was not compliant or if a required indicator was missing.
     */
    private BigDecimal riskRewardRatio;

    /**
     * Recommended number of shares based on capital at risk.
     * Null if the strategy was not compliant or if a required indicator was missing.
     */
    private Integer recommendedShares;

    /**
     * Transient list of non-blocking warnings about the risk plan.
     * Not persisted; populated during evaluation to inform the user
     * about configurations that are technically valid but potentially risky.
     */
    @Builder.Default
    private List<String> riskWarnings = List.of();
}
