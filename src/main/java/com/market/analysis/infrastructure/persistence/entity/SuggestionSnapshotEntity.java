package com.market.analysis.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "suggestion_snapshots")
@Getter
@Setter
public class SuggestionSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "strategy_id", nullable = false)
    private Long strategyId;

    @Column(name = "suggested_at", nullable = false)
    private Instant suggestedAt;

    @Column(name = "applied_filters", length = 1000)
    private String appliedFilters;

    @Column(name = "unmappable_rules", length = 4000)
    private String unmappableRules;

    @Column(name = "warnings", length = 4000)
    private String warnings;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "snapshot_id")
    private List<SuggestedTickerSnapshotEntity> suggestedTickers = new ArrayList<>();
}
