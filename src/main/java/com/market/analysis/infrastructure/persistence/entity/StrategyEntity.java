package com.market.analysis.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "strategies")
@Getter
@Setter
public class StrategyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "strategy_id")
    private List<RuleEntity> rules = new java.util.ArrayList<>();

    // Objective fields for Risk:Reward calculation
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 20)
    private ObjectiveType targetType;

    @Column(name = "target_value", precision = 19, scale = 2)
    private BigDecimal targetValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "stop_loss_type", length = 20)
    private ObjectiveType stopLossType;

    @Column(name = "stop_loss_value", precision = 19, scale = 2)
    private BigDecimal stopLossValue;

    @Column(name = "capital_to_risk", precision = 19, scale = 2)
    private BigDecimal capitalToRisk;

    @Column(name = "objective_description", length = 500)
    private String objectiveDescription;

    public enum ObjectiveType {
        SMA, PERCENTAGE, FIXED_PRICE
    }

    public void addRule(RuleEntity rule) {
        this.rules.add(rule);
    }
}