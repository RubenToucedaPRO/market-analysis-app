package com.market.analysis.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "suggested_ticker_snapshots")
@Getter
@Setter
public class SuggestedTickerSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(name = "strategy_id")
    private Long strategyId;

    @Column(name = "suggested_at")
    private Instant suggestedAt;

    @Column(name = "suitability_status", nullable = false, length = 20)
    private String suitabilityStatus;

    @Column(name = "deterministic_metrics", length = 4000)
    private String deterministicMetrics;

    @Column(name = "traceability", length = 4000)
    private String traceability;
}
