package com.market.analysis.infrastructure.external.finviz;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.market.analysis.domain.port.out.FinvizScreenerPort;

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
    private final FinvizPageFetcher pageFetcher;

    public JsoupFinvizAdapter(
            @Value("${finviz.base.url:https://finviz.com/screener.ashx}") String baseUrl,
            @Value("${finviz.user-agent:Mozilla/5.0}") String userAgent,
            @Value("${finviz.timeout-ms:8000}") int timeoutMs,
            @Value("${finviz.max-retries:1}") int maxRetries) {
        this(baseUrl, userAgent, timeoutMs, maxRetries, (url, configuredUserAgent, configuredTimeout) -> Jsoup.connect(url)
                .userAgent(configuredUserAgent)
                .timeout(configuredTimeout)
                .get());
    }

    public JsoupFinvizAdapter(String baseUrl, String userAgent, int timeoutMs, FinvizPageFetcher pageFetcher) {
        this(baseUrl, userAgent, timeoutMs, 1, pageFetcher);
    }

    public JsoupFinvizAdapter(String baseUrl, String userAgent, int timeoutMs, int maxRetries, FinvizPageFetcher pageFetcher) {
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
        this.timeoutMs = timeoutMs;
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
                .queryParam("v", "111")
                .queryParam("r", rowStart);

        if (filters != null && !filters.isBlank()) {
            builder.queryParam("f", filters);
        }
        return builder.build().encode().toUriString();
    }

    private List<String> extractTickers(Document page) {
        return page.select("a[href*=quote.ashx?t=]")
                .stream()
                .map(element -> element.text().trim().toUpperCase(Locale.ROOT))
                .filter(symbol -> !symbol.isBlank())
                .filter(symbol -> TICKER_PATTERN.matcher(symbol).matches())
                .toList();
    }

    private boolean hasNextPage(Document page, int nextStart) {
        return !page.select("a[href*=r=" + nextStart + "]").isEmpty();
    }

    @FunctionalInterface
    public interface FinvizPageFetcher {
        Document fetch(String url, String userAgent, int timeoutMs) throws IOException;
    }
}
