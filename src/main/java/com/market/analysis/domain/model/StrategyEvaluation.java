package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents the persisted result of a strategy evaluation for a ticker.
 * Immutable historical record of evaluation at a specific point in time.
 * 
 * This domain model separates the transient evaluation result (AnalysisResult)
 * from the persistent record, following Clean Architecture principles.
 */
@Getter
@Setter
@Builder
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
     * Risk:Reward ratio if strategy has an objective defined.
     * Null if strategy has no objective.
     */
    private BigDecimal riskRewardRatio;

    /**
     * Potential reward percentage based on objective.
     * Null if strategy has no objective.
     */
    private BigDecimal rewardPercentage;

    /**
     * Potential risk percentage based on objective.
     * Null if strategy has no objective.
     */
    private BigDecimal riskPercentage;
}
