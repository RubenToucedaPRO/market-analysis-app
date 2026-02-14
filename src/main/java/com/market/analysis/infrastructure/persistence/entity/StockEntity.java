package com.market.analysis.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "stocks")
@Getter
@Setter
public class StockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;

    private BigDecimal currentPrice;

    private BigDecimal openPrice;

    private BigDecimal highOfDay;

    private BigDecimal lowOfDay;

    private BigDecimal previousClose;

    private BigDecimal sma20;

    private BigDecimal sma50;

    private BigDecimal sma200;

    private Long volume;

    private Long averageVolume;

    private Instant lastUpdated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_profile_id")
    private CompanyProfileEntity companyProfile;

    @Column(name = "strategy_id")
    private Long strategyId;

    @OneToOne(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private StrategyEvaluationEntity strategyEvaluation;

}
