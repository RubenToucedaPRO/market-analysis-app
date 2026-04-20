package com.market.analysis.domain.service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.market.analysis.domain.model.ProhibitedKeyword;

public class ProhibitedKeywordMatcher {

    /**
     * Temporary fallback used during keyword migration (phase 3).
     * Must be removed after phase 6 seed/rollout when DB-managed keywords are guaranteed.
     */
    private static final List<String> FALLBACK_PROHIBITED_KEYWORDS = List.of(
            "ACQUISITION", "MERGER", "ETF", "FUND", "TRUST",
            "BULL", "BEAR", "2X", "3X",
            "THERAPEUTICS", "PHARMA", "BIO", "ONCOLOGY",
            "LP", "PARTNERS", "WARRANTS");

    public String findProhibitionReason(String companyName, List<ProhibitedKeyword> configuredKeywords) {
        if (companyName == null) {
            return null;
        }

        String normalizedCompanyName = companyName.toUpperCase(Locale.ROOT);
        List<String> activeKeywords = configuredKeywords == null ? List.of()
                : configuredKeywords.stream()
                        .filter(ProhibitedKeyword::isActive)
                        .map(ProhibitedKeyword::getKeyword)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(keyword -> !keyword.isEmpty())
                        .map(keyword -> keyword.toUpperCase(Locale.ROOT))
                        .toList();

        List<String> keywordsToEvaluate = activeKeywords.isEmpty() ? FALLBACK_PROHIBITED_KEYWORDS : activeKeywords;
        return keywordsToEvaluate.stream()
                .filter(normalizedCompanyName::contains)
                .findFirst()
                .orElse(null);
    }
}
