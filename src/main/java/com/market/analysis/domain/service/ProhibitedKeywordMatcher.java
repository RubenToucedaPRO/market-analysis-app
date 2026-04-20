package com.market.analysis.domain.service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.market.analysis.domain.model.ProhibitedKeyword;

public class ProhibitedKeywordMatcher {

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

        return activeKeywords.stream()
                .filter(normalizedCompanyName::contains)
                .findFirst()
                .orElse(null);
    }
}
