package com.market.analysis.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "strategy_rules")
@Getter
@Setter
public class RuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "subject_code")
    private String subjectCode;

    @Column(name = "subject_param")
    private Double subjectParam;

    private String operator;

    @Column(name = "target_code")
    private String targetCode;

    @Column(name = "target_param")
    private Double targetParam;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id")
    private StrategyEntity strategy;
}