package com.market.analysis.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SlowQueryInspector.
 * Tests SQL inspection and sanitization for security and observability.
 */
@DisplayName("SlowQueryInspector Unit Tests")
class SlowQueryInspectorTest {

    private SlowQueryInspector inspector;

    @BeforeEach
    void setUp() {
        inspector = new SlowQueryInspector();
    }

    @Test
    @DisplayName("Should return original SQL when inspecting simple statement")
    void testInspectReturnsOriginalSQL() {
        // Given
        String sql = "SELECT * FROM users WHERE id = 1";

        // When
        String result = inspector.inspect(sql);

        // Then
        assertThat(result).isEqualTo(sql);
    }

    @Test
    @DisplayName("Should sanitize SQL with single-quoted password")
    void testSanitizeSqlWithSingleQuotedPassword() {
        // Given
        String sql = "UPDATE users SET password = 'secret123' WHERE id = 1";

        // When
        String sanitized = inspector.sanitizeSql(sql);

        // Then
        assertThat(sanitized).contains("password='*****'");
        assertThat(sanitized).doesNotContain("secret123");
    }

    @Test
    @DisplayName("Should sanitize SQL with double-quoted token")
    void testSanitizeSqlWithDoubleQuotedToken() {
        // Given
        String sql = "INSERT INTO api_keys (token = \"abc123xyz\") VALUES (1)";

        // When
        String sanitized = inspector.sanitizeSql(sql);

        // Then
        assertThat(sanitized).contains("token=\"*****\"");
        assertThat(sanitized).doesNotContain("abc123xyz");
    }

    @Test
    @DisplayName("Should sanitize SQL with API key")
    void testSanitizeSqlWithApiKey() {
        // Given
        String sql = "SELECT * FROM config WHERE api_key = 'APIKEY12345'";

        // When
        String sanitized = inspector.sanitizeSql(sql);

        // Then
        assertThat(sanitized).contains("api_key='*****'");
        assertThat(sanitized).doesNotContain("APIKEY12345");
    }

    @Test
    @DisplayName("Should sanitize SQL with api-key (hyphenated)")
    void testSanitizeSqlWithApiKeyHyphenated() {
        // Given
        String sql = "UPDATE config SET api-key = 'KEY-789' WHERE id = 1";

        // When
        String sanitized = inspector.sanitizeSql(sql);

        // Then
        assertThat(sanitized).contains("api-key='*****'");
        assertThat(sanitized).doesNotContain("KEY-789");
    }

    @Test
    @DisplayName("Should sanitize SQL with secret")
    void testSanitizeSqlWithSecret() {
        // Given
        String sql = "SELECT * FROM vault WHERE secret = 'topsecret'";

        // When
        String sanitized = inspector.sanitizeSql(sql);

        // Then
        assertThat(sanitized).contains("secret='*****'");
        assertThat(sanitized).doesNotContain("topsecret");
    }

    @Test
    @DisplayName("Should normalize whitespace in SQL")
    void testSanitizeSqlNormalizesWhitespace() {
        // Given
        String sql = "SELECT  *  \n  FROM   users\t\tWHERE   id = 1";

        // When
        String sanitized = inspector.sanitizeSql(sql);

        // Then
        assertThat(sanitized).doesNotContain("\n");
        assertThat(sanitized).doesNotContain("\t");
        assertThat(sanitized).doesNotContain("  "); // No double spaces
    }

    @Test
    @DisplayName("Should truncate very long SQL statements")
    void testSanitizeSqlTruncatesLongStatements() {
        // Given
        String longSql = "SELECT " + "column, ".repeat(200) + "id FROM users";

        // When
        String sanitized = inspector.sanitizeSql(longSql);

        // Then
        assertThat(sanitized.length()).isLessThanOrEqualTo(503); // 500 + "..."
        assertThat(sanitized).endsWith("...");
    }

    @Test
    @DisplayName("Should not truncate SQL shorter than max length")
    void testSanitizeSqlDoesNotTruncateShortStatements() {
        // Given
        String shortSql = "SELECT * FROM users WHERE id = 1";

        // When
        String sanitized = inspector.sanitizeSql(shortSql);

        // Then
        assertThat(sanitized).isEqualTo(shortSql);
        assertThat(sanitized).doesNotEndWith("...");
    }

    @Test
    @DisplayName("Should handle SQL with mixed sensitive patterns")
    void testSanitizeSqlWithMixedPatterns() {
        // Given
        String sql = "UPDATE config SET password = 'pass123', api_key = 'key456', token = 'tok789' WHERE id = 1";

        // When
        String sanitized = inspector.sanitizeSql(sql);

        // Then
        assertThat(sanitized).contains("password='*****'");
        assertThat(sanitized).contains("api_key='*****'");
        assertThat(sanitized).contains("token='*****'");
        assertThat(sanitized).doesNotContain("pass123");
        assertThat(sanitized).doesNotContain("key456");
        assertThat(sanitized).doesNotContain("tok789");
    }

    @Test
    @DisplayName("Should handle SQL without sensitive data")
    void testSanitizeSqlWithoutSensitiveData() {
        // Given
        String sql = "SELECT id, name, email FROM users WHERE active = true";

        // When
        String sanitized = inspector.sanitizeSql(sql);

        // Then
        assertThat(sanitized).isEqualTo(sql);
    }

    @Test
    @DisplayName("Should be case-insensitive when sanitizing")
    void testSanitizeSqlCaseInsensitive() {
        // Given
        String sql = "UPDATE users SET PASSWORD = 'pass' AND Token = 'tok' WHERE id = 1";

        // When
        String sanitized = inspector.sanitizeSql(sql);

        // Then
        assertThat(sanitized).contains("PASSWORD='*****'");
        assertThat(sanitized).contains("Token='*****'");
        assertThat(sanitized).doesNotContain("pass");
        assertThat(sanitized).doesNotContain("tok");
    }

    @Test
    @DisplayName("Should have correct slow query threshold constant")
    void testSlowQueryThresholdConstant() {
        // Then
        assertThat(SlowQueryInspector.SLOW_QUERY_THRESHOLD_MS).isEqualTo(500L);
    }
}
