package com.market.analysis.domain.model;

import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;

    public static ProhibitedTicker createProhibited(String ticker, String reason) {
        return ProhibitedTicker.builder()
                .ticker(ticker)
                .reason(reason)
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }
}
