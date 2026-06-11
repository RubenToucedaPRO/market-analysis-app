package com.market.analysis.presentation.util;

/**
 * Presentation-layer constants shared across controllers and exception handlers.
 * Centralises magic strings used as model/flash attribute keys, template names
 * and redirect URLs.
 */
public final class WebConstants {

    // ── Model Attributes ────────────────────────────────────────────────
    public static final String ATTR_RULE_DEFINITIONS = "ruleDefinitions";
    public static final String ATTR_RULE_DEFINITION = "ruleDefinition";
    public static final String ATTR_IS_EDIT = "isEdit";
    public static final String ATTR_CAPABILITIES = "capabilities";
    public static final String ATTR_STRATEGIES = "strategies";
    public static final String ATTR_STRATEGY = "strategy";
    public static final String ATTR_TICKERS = "tickers";
    public static final String ATTR_TICKER = "ticker";
    public static final String ATTR_SUGGESTED_TICKERS = "suggestedTickers";
    public static final String ATTR_DISCARDED_TICKERS = "discardedTickers";
    public static final String ATTR_UNMAPPABLE_RULES = "unmappableRules";
    public static final String ATTR_SUGGESTED_AT = "suggestedAt";
    public static final String ATTR_PROHIBITED_TICKERS = "prohibitedTickers";
    public static final String ATTR_PROHIBITED_KEYWORDS = "prohibitedKeywords";

    // ── Flash Attributes ────────────────────────────────────────────────
    public static final String UI_NOTIFICATION_KEY = "uiNotification";

    // ── Template Names ──────────────────────────────────────────────────
    public static final String TEMPLATE_ANALYSIS = "analysis/analysis";
    public static final String TEMPLATE_TICKER_DETAIL = "analysis/ticker-detail";
    public static final String TEMPLATE_TICKER_CHART = "analysis/ticker-chart";
    public static final String TEMPLATE_STRATEGIES_LIST = "strategies/list";
    public static final String TEMPLATE_STRATEGIES_CREATE = "strategies/create";
    public static final String TEMPLATE_STRATEGIES_DETAIL = "strategies/detail";
    public static final String TEMPLATE_RULE_DEFINITIONS_LIST = "rule-definitions/list";
    public static final String TEMPLATE_RULE_DEFINITIONS_CREATE = "rule-definitions/create";
    public static final String TEMPLATE_PROHIBITED_TICKERS_LIST = "prohibited-tickers/list";
    public static final String TEMPLATE_ERROR = "error";
    public static final String TEMPLATE_HOME = "home";
    public static final String TEMPLATE_LOGIN = "login";

    // ── Redirect URLs ───────────────────────────────────────────────────
    public static final String REDIRECT_ANALYSIS = "redirect:/analysis";
    public static final String REDIRECT_STRATEGIES = "redirect:/strategies";
    public static final String REDIRECT_STRATEGIES_PREFIX = "redirect:/strategies/";
    public static final String REDIRECT_RULE_DEFINITIONS = "redirect:/rule-definitions";
    public static final String REDIRECT_PROHIBITED_TICKERS = "redirect:/prohibited-tickers";

    // ── Error View Attributes ───────────────────────────────────────────
    public static final String ATTR_ERROR_MESSAGE = "errorMessage";
    public static final String ATTR_ERROR_DETAILS = "errorDetails";
    public static final String ATTR_ERROR_TYPE = "errorType";
    public static final String DEFAULT_REFERER = "/";

    private WebConstants() {
        // utility class – no instantiation
    }
}
