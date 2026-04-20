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
public class ProhibitedKeyword {

    private String keyword;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProhibitedKeyword createActive(String keyword) {
        Instant now = Instant.now();
        return ProhibitedKeyword.builder()
                .keyword(keyword)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
