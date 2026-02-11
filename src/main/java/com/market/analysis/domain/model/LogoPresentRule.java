package com.market.analysis.domain.model;

/**
 * Validation rule that checks if the stock has a company logo URL.
 * This indicates that the company profile was successfully retrieved.
 */
public class LogoPresentRule implements ValidationRule {

    private static final String RULE_ID = "LOGO_PRESENT";
    private static final String RULE_NAME = "Logo Present";
    private static final String DESCRIPTION = "Validates that the stock has a company logo";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }

    @Override
    public boolean evaluate(Stock stock) {
        if (stock == null) {
            return false;
        }

        String logoUrl = stock.getLogoUrl();
        return logoUrl != null && !logoUrl.trim().isEmpty();
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
