package com.market.analysis.unit.presentation.template;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@DisplayName("Prohibited tickers template security tests")
class ProhibitedTickersTemplateSecurityTest {

    private static final Path TEMPLATE_PATH = Path.of(
            "src/main/resources/templates/prohibited-tickers/list.html");
    // POST forms in this template: ticker deletion, keyword creation, keyword deletion.
    private static final int EXPECTED_CSRF_FIELDS_COUNT = 3;

    @Test
    @DisplayName("Should not use unescaped rendering in template")
    void shouldNotUseUnescapedRenderingInTemplate() throws IOException {
        String template = Files.readString(TEMPLATE_PATH);

        assertThat(template).contains("th:text=");
        assertThat(template).doesNotContain("th:utext=");
    }

    @Test
    @DisplayName("Should include CSRF hidden field in every POST form")
    void shouldIncludeCsrfFieldInEveryPostForm() throws IOException {
        String template = Files.readString(TEMPLATE_PATH);
        Document document = Jsoup.parse(template);

        List<Element> postForms = document.select("form[method=post]");
        assertThat(postForms).hasSize(EXPECTED_CSRF_FIELDS_COUNT);
        assertThat(postForms).allMatch(form -> form.html().contains("th:name=\"${_csrf.parameterName}\"")
                && form.html().contains("th:value=\"${_csrf.token}\""));
    }
}
