package com.market.analysis.unit.infrastructure.external.finviz;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.infrastructure.external.finviz.JsoupFinvizAdapter;

@DisplayName("JsoupFinvizAdapter Unit Tests")
class JsoupFinvizAdapterTest {

    @Test
    @DisplayName("Should paginate, deduplicate symbols and stop when max results is reached")
    void shouldPaginateAndDeduplicate() {
        List<String> requestedUrls = new ArrayList<>();
        List<String> usedUserAgents = new ArrayList<>();
        List<Integer> usedTimeouts = new ArrayList<>();
        JsoupFinvizAdapter adapter = new JsoupFinvizAdapter(
                "https://finviz.com/screener.ashx",
                "test-agent",
                5000,
                (url, userAgent, timeoutMs) -> {
                    requestedUrls.add(url);
                    usedUserAgents.add(userAgent);
                    usedTimeouts.add(timeoutMs);
                    if (url.contains("r=1")) {
                        return parseFixture("fixtures/finviz/screener-page-1.html");
                    }
                    if (url.contains("r=21")) {
                        return parseFixture("fixtures/finviz/screener-page-2.html");
                    }
                    return parseFixture("fixtures/finviz/screener-page-3.html");
                });

        List<String> tickers = adapter.findTickers("ta_sma20_pa", 5);

        assertThat(tickers).containsExactly("AAPL", "MSFT", "GOOGL", "NVDA", "AMZN");
        assertThat(requestedUrls).anyMatch(url -> url.contains("&r=21"));
        assertThat(requestedUrls).anyMatch(url -> url.contains("&r=41"));
        assertThat(requestedUrls).allMatch(url -> url.contains("f=ta_sma20_pa"));
        assertThat(usedUserAgents).containsOnly("test-agent");
        assertThat(usedTimeouts).containsOnly(5000);
    }

    @Test
    @DisplayName("Should handle network errors by returning collected results")
    void shouldHandleNetworkErrors() {
        JsoupFinvizAdapter adapter = new JsoupFinvizAdapter(
                "https://finviz.com/screener.ashx",
                "test-agent",
                5000,
                (url, userAgent, timeoutMs) -> {
                    if (url.contains("r=1")) {
                        return parseFixture("fixtures/finviz/screener-page-1.html");
                    }
                    throw new IOException("Connection reset");
                });

        List<String> tickers = adapter.findTickers("ta_sma20_pa", 10);

        assertThat(tickers).containsExactly("AAPL", "MSFT");
    }

    @Test
    @DisplayName("Should return empty list when structure changes and symbols cannot be extracted")
    void shouldReturnEmptyWhenStructureChanges() {
        JsoupFinvizAdapter adapter = new JsoupFinvizAdapter(
                "https://finviz.com/screener.ashx",
                "test-agent",
                5000,
                (url, userAgent, timeoutMs) -> parseFixture("fixtures/finviz/screener-structure-changed.html"));

        List<String> tickers = adapter.findTickers("ta_sma20_pa", 5);

        assertThat(tickers).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when max results is zero")
    void shouldReturnEmptyWhenMaxResultsIsZero() {
        JsoupFinvizAdapter adapter = new JsoupFinvizAdapter(
                "https://finviz.com/screener.ashx",
                "test-agent",
                5000,
                (url, userAgent, timeoutMs) -> parseFixture("fixtures/finviz/screener-page-1.html"));

        List<String> tickers = adapter.findTickers("ta_sma20_pa", 0);

        assertThat(tickers).isEmpty();
    }

    private static Document parseFixture(String resourcePath) throws IOException {
        try (InputStream input = JsoupFinvizAdapterTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IOException("Fixture not found: " + resourcePath);
            }
            String html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return Jsoup.parse(html);
        }
    }
}
