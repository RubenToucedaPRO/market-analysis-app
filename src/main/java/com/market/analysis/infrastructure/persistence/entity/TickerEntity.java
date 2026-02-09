package com.market.analysis.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tickers")
@Getter
@Setter
public class TickerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
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

    private LocalDateTime lastUpdated;

}
