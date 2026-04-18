package com.market.analysis.infrastructure.persistence.entity;

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

    @Column(name = "suitability_status", nullable = false, length = 20)
    private String suitabilityStatus;

    @Column(name = "traceability", length = 4000)
    private String traceability;
}
