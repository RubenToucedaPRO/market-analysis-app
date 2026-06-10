package com.market.analysis.infrastructure.persistence.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 * Embeddable JPA component for persisting strategy objective configuration.
 * Contains target, stop-loss, and capital risk parameters embedded in StrategyEntity.
 */
@Embeddable
@Getter
@Setter
public class StrategyObjectiveEntity {

    @Column(name = "objective_target_type", nullable = true)
    private String targetType;

    @Column(name = "objective_target_value", nullable = true, precision = 19, scale = 4)
    private BigDecimal targetValue;

    @Column(name = "objective_stop_loss_type", nullable = true)
    private String stopLossType;

    @Column(name = "objective_stop_loss_value", nullable = true, precision = 19, scale = 4)
    private BigDecimal stopLossValue;

    @Column(name = "objective_capital_to_risk", nullable = true, precision = 19, scale = 4)
    private BigDecimal capitalToRisk;

    @Column(name = "objective_description", nullable = true, length = 500)
    private String description;
}
