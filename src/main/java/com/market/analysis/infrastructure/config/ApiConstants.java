package com.market.analysis.infrastructure.config;

/**
 * Centralised constants for external API communication.
 * Groups endpoint paths, query parameter names, CSS selectors
 * and other infrastructure-level magic strings.
 */
public final class ApiConstants {

    private ApiConstants() {
        // utility class – no instantiation
    }

    // ── Finnhub ───────────────────────────────────────────────────────
    public static final String FINNHUB_ENDPOINT_QUOTE = "/quote";
    public static final String FINNHUB_ENDPOINT_PROFILE = "/stock/profile2";

    // ── Polygon ───────────────────────────────────────────────────────
    public static final String POLYGON_URI_AGGREGATES = "v2/aggs/ticker/{ticker}/range/1/day/{from}/{to}";
    public static final String POLYGON_QUERY_ADJUSTED = "adjusted";
    public static final String POLYGON_QUERY_SORT = "sort";
    public static final String POLYGON_SORT_DESC = "desc";
    public static final String POLYGON_QUERY_LIMIT = "limit";
    public static final String POLYGON_QUERY_API_KEY = "apiKey";
    public static final String POLYGON_DATE_PATTERN = "yyyy-MM-dd";

    // ── Polygon JSON fields ───────────────────────────────────────────
    public static final String JSON_RESULTS = "results";
    public static final String JSON_CLOSE = "c";
    public static final String JSON_VOLUME = "v";
    public static final String JSON_TIMESTAMP = "t";
    public static final String JSON_OPEN = "o";
    public static final String JSON_HIGH = "h";
    public static final String JSON_LOW = "l";

    // ── Finviz ────────────────────────────────────────────────────────
    public static final String FINVIZ_QUERY_VIEW = "v";
    public static final String FINVIZ_VIEW_VALUE = "111";
    public static final String FINVIZ_QUERY_ROW = "r";
    public static final String FINVIZ_QUERY_FILTERS = "f";
    public static final String FINVIZ_SELECTOR_TABLE_ROWS = "tbody tr";
    public static final String FINVIZ_SELECTOR_TICKER_LINK = "a.tab-link, a[href*=ashx?t=]";
    public static final String FINVIZ_SELECTOR_NEXT_PAGE = "a[href*=r=";

    // ── OpenRouter ────────────────────────────────────────────────────
    public static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1";
    public static final String OPENROUTER_HEADER_REFERER = "HTTP-Referer";
    public static final String OPENROUTER_DEFAULT_REFERER = "http://localhost:8080";

    // ── HTTP Headers ──────────────────────────────────────────────────
    public static final String HEADER_REFERER = "Referer";
}
