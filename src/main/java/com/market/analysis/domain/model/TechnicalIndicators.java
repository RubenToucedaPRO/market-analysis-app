package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class TechnicalIndicators {

    BigDecimal sma20;

    BigDecimal sma50;

    BigDecimal sma200;

    Long currentVolume;

    Long averageVolume;

    Instant lastUpdated;
}
