package com.market.analysis.domain.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProhibitedTicker {

    private String ticker;
    private String reason;
    private Instant createdAt;

    public static ProhibitedTicker createProhibited(String ticker, String reason) {
        return ProhibitedTicker.builder()
                .ticker(ticker)
                .reason(reason)
                .createdAt(java.time.Instant.now())
                .build();
    }
}
