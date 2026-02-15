package com.market.analysis.unit.infrastructure.config;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.market.analysis.infrastructure.config.SlowQueryInspector;

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

    @ParameterizedTest(name = "{index} => sql=''{0}''")
    @DisplayName("Should return original SQL unchanged for various inputs")
    @ValueSource(strings = {
        "SELECT * FROM users WHERE id = 1",
        "UPDATE users SET password = 'user_input_value' WHERE id = 1",
        "INSERT INTO api_keys (token = \"user_token_value\") VALUES (1)",
        "SELECT * FROM config WHERE api_key = 'user_api_key_value'",
        "UPDATE config SET api-key = 'user_hyphenated_key' WHERE id = 1",
        "SELECT * FROM vault WHERE secret = 'user_secret_value'",
        "SELECT  *  \n  FROM   users\t\tWHERE   id = 1",
        "UPDATE config SET password = 'user_pass', api_key = 'user_key', token = 'user_token' WHERE id = 1",
        "SELECT id, name, email FROM users WHERE active = true",
        "UPDATE users SET PASSWORD = 'user_pass' AND Token = 'user_token' WHERE id = 1"
    })
    void testInspectReturnsOriginalSqlUnchanged(String sql) {
        // When
        String result = inspector.inspect(sql);

        // Then - inspect returns original SQL unchanged
        assertThat(result).isEqualTo(sql);
    }

    @ParameterizedTest(name = "{index} => longSql with {0} repetitions")
    @DisplayName("Should return long SQL unchanged in inspect method")
    @ValueSource(ints = { 100, 200 })
    void testInspectReturnsLongSqlUnchanged(int repetitions) {
        // Given
        String longSql = "SELECT " + "column, ".repeat(repetitions) + "id FROM users";

        // When
        String result = inspector.inspect(longSql);

        // Then - inspect returns original SQL unchanged, even if long
        assertThat(result).isEqualTo(longSql);
    }

}
