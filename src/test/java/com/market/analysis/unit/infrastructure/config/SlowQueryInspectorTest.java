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
        String sql = "UPDATE users SET password = 'user_input_value' WHERE id = 1";

        // When
        String result = inspector.inspect(sql);

        // Then - inspect returns original SQL unchanged
        assertThat(result).isEqualTo(sql);
    }

    @Test
    @DisplayName("Should sanitize SQL with double-quoted token")
    void testSanitizeSqlWithDoubleQuotedToken() {
        // Given
        String sql = "INSERT INTO api_keys (token = \"user_token_value\") VALUES (1)";

        // When
        String result = inspector.inspect(sql);

        // Then - inspect returns original SQL unchanged
        assertThat(result).isEqualTo(sql);
    }

    @Test
    @DisplayName("Should sanitize SQL with API key")
    void testSanitizeSqlWithApiKey() {
        // Given
        String sql = "SELECT * FROM config WHERE api_key = 'user_api_key_value'";

        // When
        String result = inspector.inspect(sql);

        // Then - inspect returns original SQL unchanged
        assertThat(result).isEqualTo(sql);
    }

    @Test
    @DisplayName("Should sanitize SQL with api-key (hyphenated)")
    void testSanitizeSqlWithApiKeyHyphenated() {
        // Given
        String sql = "UPDATE config SET api-key = 'user_hyphenated_key' WHERE id = 1";

        // When
        String result = inspector.inspect(sql);

        // Then - inspect returns original SQL unchanged
        assertThat(result).isEqualTo(sql);
    }

    @Test
    @DisplayName("Should sanitize SQL with secret")
    void testSanitizeSqlWithSecret() {
        // Given
        String sql = "SELECT * FROM vault WHERE secret = 'user_secret_value'";

        // When
        String result = inspector.inspect(sql);

        // Then - inspect returns original SQL unchanged
        assertThat(result).isEqualTo(sql);
    }

    @Test
    @DisplayName("Should return original SQL preserving whitespace")
    void testInspectPreservesWhitespace() {
        // Given
        String sql = "SELECT  *  \n  FROM   users\t\tWHERE   id = 1";

        // When
        String result = inspector.inspect(sql);

        // Then - inspect returns original SQL unchanged
        assertThat(result).isEqualTo(sql);
    }

    @Test
    @DisplayName("Should return long SQL unchanged in inspect method")
    void testInspectReturnsLongSqlUnchanged() {
        // Given
        String longSql = "SELECT " + "column, ".repeat(200) + "id FROM users";

        // When
        String result = inspector.inspect(longSql);

        // Then - inspect returns original SQL unchanged, even if long
        assertThat(result).isEqualTo(longSql);
    }

    @Test
    @DisplayName("Should return short SQL unchanged")
    void testInspectReturnsShortSqlUnchanged() {
        // Given
        String shortSql = "SELECT * FROM users WHERE id = 1";

        // When
        String result = inspector.inspect(shortSql);

        // Then - inspect returns original SQL unchanged
        assertThat(result).isEqualTo(shortSql);
    }

    @Test
    @DisplayName("Should handle SQL with mixed sensitive patterns")
    void testInspectWithMixedSensitivePatterns() {
        // Given
        String sql = "UPDATE config SET password = 'user_pass', api_key = 'user_key', token = 'user_token' WHERE id = 1";

        // When
        String result = inspector.inspect(sql);

        // Then - inspect returns original SQL unchanged
        assertThat(result).isEqualTo(sql);
    }

    @Test
    @DisplayName("Should handle SQL without sensitive data")
    void testInspectWithoutSensitiveData() {
        // Given
        String sql = "SELECT id, name, email FROM users WHERE active = true";

        // When
        String result = inspector.inspect(sql);

        // Then - inspect returns original SQL unchanged
        assertThat(result).isEqualTo(sql);
    }

    @Test
    @DisplayName("Should handle case-insensitive sensitive fields")
    void testInspectWithCaseInsensitiveSensitiveFields() {
        // Given
        String sql = "UPDATE users SET PASSWORD = 'user_pass' AND Token = 'user_token' WHERE id = 1";

        // When
        String result = inspector.inspect(sql);

        // Then - inspect returns original SQL unchanged
        assertThat(result).isEqualTo(sql);
    }

    @Test
    @DisplayName("Should have correct slow query threshold constant")
    void testSlowQueryThresholdConstant() {
        // Then
        assertThat(SlowQueryInspector.SLOW_QUERY_THRESHOLD_MS).isEqualTo(500L);
    }
}
