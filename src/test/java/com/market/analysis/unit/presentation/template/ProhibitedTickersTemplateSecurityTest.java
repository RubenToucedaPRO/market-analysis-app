package com.market.analysis.unit.presentation.template;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Prohibited tickers template security tests")
class ProhibitedTickersTemplateSecurityTest {

    private static final Path TEMPLATE_PATH = Path.of(
            "src/main/resources/templates/prohibited-tickers/list.html");

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

        assertThat(template)
                .contains("th:name=\"${_csrf.parameterName}\"")
                .contains("th:value=\"${_csrf.token}\"");
        assertThat(template.split("th:name=\"\\$\\{_csrf.parameterName\\}\"", -1).length - 1)
                .isGreaterThanOrEqualTo(3);
    }
}
