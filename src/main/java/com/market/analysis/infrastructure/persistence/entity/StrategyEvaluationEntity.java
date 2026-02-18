package com.market.analysis.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity for persisting strategy evaluation results.
 * 
 * Represents a historical record of a strategy evaluation at a specific point
 * in time.
 * Includes snapshot data and evaluation metrics for audit trail and historical
 * analysis.
 */
@Entity
@Table(name = "strategy_evaluations")
@Getter
@Setter
public class StrategyEvaluationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "stock_id", referencedColumnName = "id", nullable = false)
    private StockEntity stock;

    @Column(name = "strategy_name", nullable = false)
    private String strategyName;

    @Column(nullable = false)
    private boolean compliant;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal complianceRate;

    @Column(length = 2000)
    private String summary;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    @Column(name = "price_at_evaluation", precision = 19, scale = 2)
    private BigDecimal priceAtEvaluation;

    @Column(name = "latest", nullable = false)
    private boolean latest;

    @Column(name = "risk_reward_ratio", precision = 10, scale = 2)
    private BigDecimal riskRewardRatio;

    @Column(name = "reward_percentage", precision = 10, scale = 4)
    private BigDecimal rewardPercentage;

    @Column(name = "risk_percentage", precision = 10, scale = 4)
    private BigDecimal riskPercentage;

}
