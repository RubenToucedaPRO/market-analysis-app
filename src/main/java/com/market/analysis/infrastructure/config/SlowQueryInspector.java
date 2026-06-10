package com.market.analysis.infrastructure.config;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.lang.NonNull;

import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate 6 StatementInspector implementation for detecting and logging slow
 * database queries.
 * 
 * <p>
 * This inspector provides SQL sanitization for security and works in
 * conjunction with
 * Hibernate's built-in slow query logging (configured via properties). The
 * StatementInspector
 * intercepts SQL statements before execution, while the actual timing is
 * handled by
 * configuring:
 * spring.jpa.properties.hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS=500
 * </p>
 * 
 * <p>
 * Architecture: This component belongs to the Infrastructure layer and provides
 * centralized database observability without polluting domain or application
 * layers,
 * respecting the Hexagonal Architecture principles.
 * </p>
 * 
 * <p>
 * Security: SQL statements are sanitized to prevent exposure of sensitive data
 * such as passwords, tokens, or API keys in logs.
 * </p>
 * 
 * @since 1.0.0
 */
@Slf4j
public class SlowQueryInspector implements StatementInspector {

    private static final int MAX_SQL_LOG_LENGTH = 2000;
    private static final String SQL_WHITESPACE_PATTERN = "\\s+";
    private static final String SENSITIVE_FIELD_PATTERN = "(?i)(password|token|secret|api[_-]?key)\\s*=\\s*['\"][^'\"]*['\"]";
    private static final String OBFUSCATION_REPLACEMENT = "$1='*****'";
    private static final String TRUNCATION_SUFFIX = "...";

    @Override
    @NonNull
    public String inspect(@NonNull String sql) {
        if (log.isDebugEnabled()) {
            log.debug("SQL to execute: {}", sanitizeSql(sql));
        }
        return sql;
    }

    public String sanitizeSql(@NonNull String sql) {
        // 1. Whitespace cleanup (Java 21 optimized)
        String sanitized = sql.replaceAll(SQL_WHITESPACE_PATTERN, " ").trim();

        // 2. Security obfuscation (Key for LOPD compliance in TFM)
        sanitized = sanitized.replaceAll(SENSITIVE_FIELD_PATTERN, OBFUSCATION_REPLACEMENT);

        return sanitized.length() > MAX_SQL_LOG_LENGTH
                ? sanitized.substring(0, MAX_SQL_LOG_LENGTH) + TRUNCATION_SUFFIX
                : sanitized;
    }
}
