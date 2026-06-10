package com.market.analysis.infrastructure.external.finviz;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.market.analysis.domain.port.out.FinvizScreenerPort;
import com.market.analysis.infrastructure.config.ApiConstants;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JsoupFinvizAdapter implements FinvizScreenerPort {

    private static final int PAGE_SIZE = 20;
    private static final Pattern TICKER_PATTERN = Pattern.compile("^[A-Z][A-Z0-9.-]{0,9}$");

    private final String baseUrl;
    private final String userAgent;
    private final int timeoutMs;
    private final int maxRetries;
    private final String defaultCountryFilter;
    private final String defaultIndustryFilter;
    private final FinvizPageFetcher pageFetcher;

    @Autowired
    public JsoupFinvizAdapter(
            @Value("${finviz.base.url:https://finviz.com/screener.ashx}") String baseUrl,
            @Value("${finviz.user-agent:Mozilla/5.0}") String userAgent,
            @Value("${finviz.timeout-ms:8000}") int timeoutMs,
            @Value("${finviz.max-retries:1}") int maxRetries,
            @Value("${finviz.default.country-filter:geo_usa}") String defaultCountryFilter,
            @Value("${finviz.default.industry-filter:ind_stocksonly}") String defaultIndustryFilter) {
        this(baseUrl, userAgent, timeoutMs, maxRetries, defaultCountryFilter, defaultIndustryFilter, (url, configuredUserAgent, configuredTimeout) -> Jsoup.connect(url)
                .userAgent(configuredUserAgent)
                .timeout(configuredTimeout)
                .get());
    }

    public JsoupFinvizAdapter(String baseUrl, String userAgent, int timeoutMs, int maxRetries,
            String defaultCountryFilter, String defaultIndustryFilter, FinvizPageFetcher pageFetcher) {
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
        this.timeoutMs = timeoutMs;
        this.defaultCountryFilter = defaultCountryFilter;
        this.defaultIndustryFilter = defaultIndustryFilter;
        if (maxRetries < 0) {
            log.warn("finviz_invalid_retry_config providedMaxRetries={} effectiveMaxRetries=0", maxRetries);
        }
        this.maxRetries = Math.max(0, maxRetries);
        this.pageFetcher = pageFetcher;
    }

    @Override
    public List<String> findTickers(String filters, int maxResults) {
        if (maxResults <= 0) {
            return List.of();
        }

        Set<String> uniqueTickers = new LinkedHashSet<>();
        int rowStart = 1;

        while (uniqueTickers.size() < maxResults) {
            String requestUrl = buildUrl(filters, rowStart);
            Document page = fetchPageWithRetry(requestUrl, rowStart);
            if (page == null) {
                break;
            }

            List<String> pageTickers = extractTickers(page);
            if (pageTickers.isEmpty()) {
                log.info("finviz_screener_empty_page rowStart={} url={}", rowStart, requestUrl);
                break;
            }

            int before = uniqueTickers.size();
            uniqueTickers.addAll(pageTickers);
            if (uniqueTickers.size() >= maxResults) {
                break;
            }

            int nextStart = rowStart + PAGE_SIZE;
            boolean hasExplicitNext = hasNextPage(page, nextStart);
            boolean pageLooksFull = pageTickers.size() >= PAGE_SIZE;
            if (!hasExplicitNext && !pageLooksFull) {
                break;
            }

            if (before == uniqueTickers.size() && !hasExplicitNext) {
                break;
            }

            rowStart = nextStart;
        }

        return uniqueTickers.stream()
                .limit(maxResults)
                .toList();
    }

    private Document fetchPageWithRetry(String requestUrl, int rowStart) {
        int totalAttempts = maxRetries + 1;
        for (int attempt = 1; attempt <= totalAttempts; attempt++) {
            try {
                return pageFetcher.fetch(requestUrl, userAgent, timeoutMs);
            } catch (IOException ex) {
                log.warn(
                        "finviz_request_failed rowStart={} attempt={} totalAttempts={} timeoutMs={} message={}",
                        rowStart,
                        attempt,
                        totalAttempts,
                        timeoutMs,
                        ex.getMessage());
            }
        }
        return null;
    }

    private String buildUrl(String filters, int rowStart) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam(ApiConstants.FINVIZ_QUERY_VIEW, ApiConstants.FINVIZ_VIEW_VALUE)
                .queryParam(ApiConstants.FINVIZ_QUERY_ROW, rowStart);

        String effectiveFilters = buildEffectiveFilters(filters);
        if (!effectiveFilters.isBlank()) {
            builder.queryParam(ApiConstants.FINVIZ_QUERY_FILTERS, effectiveFilters);
        }
        return builder.build().encode().toUriString();
    }

    private String buildEffectiveFilters(String filters) {
        if (filters == null || filters.isBlank()) {
            return defaultCountryFilter + "," + defaultIndustryFilter;
        }
        return defaultCountryFilter + "," + defaultIndustryFilter + "," + filters.trim();
    }

    private List<String> extractTickers(Document page) {
        return page.select(ApiConstants.FINVIZ_SELECTOR_TABLE_ROWS)
                .stream()
                .map(this::extractTickerFromRow)
                .filter(symbol -> symbol != null)
                .filter(symbol -> !symbol.isBlank())
                .filter(symbol -> TICKER_PATTERN.matcher(symbol).matches())
                .toList();
    }

    private String extractTickerFromRow(Element row) {
        List<Element> columns = row.select("td");
        if (columns.size() < 2) {
            return null;
        }
        Element tickerLink = columns.get(1).selectFirst(ApiConstants.FINVIZ_SELECTOR_TICKER_LINK);
        if (tickerLink == null) {
            return null;
        }
        return tickerLink.text().trim().toUpperCase(Locale.ROOT);
    }

    private boolean hasNextPage(Document page, int nextStart) {
        return !page.select(ApiConstants.FINVIZ_SELECTOR_NEXT_PAGE + nextStart + "]").isEmpty();
    }

    @FunctionalInterface
    public interface FinvizPageFetcher {
        Document fetch(String url, String userAgent, int timeoutMs) throws IOException;
    }
}
