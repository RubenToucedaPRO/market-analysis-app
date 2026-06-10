package com.market.analysis.unit.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.ProhibitedKeyword;
import com.market.analysis.domain.service.ProhibitedKeywordMatcher;

@DisplayName("ProhibitedKeywordMatcher Domain Service Tests")
class ProhibitedKeywordMatcherTest {

    private final ProhibitedKeywordMatcher matcher = new ProhibitedKeywordMatcher();

    @Test
    @DisplayName("Should use configured active keywords when available")
    void shouldUseConfiguredActiveKeywordsWhenAvailable() {
        List<ProhibitedKeyword> configuredKeywords = List.of(
                ProhibitedKeyword.builder().keyword("ETF").active(true).build(),
                ProhibitedKeyword.builder().keyword("FUND").active(false).build());

        String reason = matcher.findProhibitionReason("SPDR S&P 500 ETF Trust", configuredKeywords);

        assertEquals("ETF", reason);
    }

    @Test
    @DisplayName("Should return null when configured keyword list is empty")
    void shouldReturnNullWhenConfiguredKeywordListIsEmpty() {
        String reason = matcher.findProhibitionReason("Vanguard International Stock Index Fund", List.of());

        assertNull(reason);
    }

    @Test
    @DisplayName("Should return null when company name is null")
    void shouldReturnNullWhenCompanyNameIsNull() {
        String reason = matcher.findProhibitionReason(null, List.of(
                ProhibitedKeyword.builder().keyword("ETF").active(true).build()));

        assertNull(reason);
    }

    @Test
    @DisplayName("Should match configured keywords case-insensitively")
    void shouldMatchConfiguredKeywordsCaseInsensitively() {
        List<ProhibitedKeyword> configuredKeywords = List.of(
                ProhibitedKeyword.builder().keyword("etf").active(true).build());

        String reason = matcher.findProhibitionReason("spdr s&p 500 EtF trust", configuredKeywords);

        assertEquals("ETF", reason);
    }
}
