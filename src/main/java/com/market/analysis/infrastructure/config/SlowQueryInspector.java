package com.market.analysis.infrastructure.config;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.lang.NonNull;

import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate 6 StatementInspector implementation for detecting and logging slow database queries.
 * 
 * <p>This inspector provides SQL sanitization for security and works in conjunction with
 * Hibernate's built-in slow query logging (configured via properties). The StatementInspector
 * intercepts SQL statements before execution, while the actual timing is handled by
 * configuring: spring.jpa.properties.hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS=500</p>
 * 
 * <p>Architecture: This component belongs to the Infrastructure layer and provides
 * centralized database observability without polluting domain or application layers,
 * respecting the Hexagonal Architecture principles.</p>
 * 
 * <p>Security: SQL statements are sanitized to prevent exposure of sensitive data
 * such as passwords, tokens, or API keys in logs.</p>
 * 
 * @since 1.0.0
 */
@Slf4j
public class SlowQueryInspector implements StatementInspector {

    /**
     * Threshold in milliseconds for considering a query as "slow".
     * This constant documents the threshold configured in application properties.
     */
    static final long SLOW_QUERY_THRESHOLD_MS = 500L;

    /**
     * Maximum length of SQL statement to log (prevents excessive log size).
     */
    private static final int MAX_SQL_LOG_LENGTH = 500;

    /**
     * Inspects SQL statements before execution and sanitizes them for security.
     * 
     * <p>This method is called by Hibernate before each SQL statement is sent to the database.
     * It returns the original SQL for execution but also triggers sanitization logging
     * when debug logging is enabled.</p>
     * 
     * @param sql the SQL statement about to be executed
     * @return the same SQL statement (unmodified for execution)
     */
    @Override
    @NonNull
    public String inspect(@NonNull String sql) {
        // Log sanitized SQL at debug level for inspection
        if (log.isDebugEnabled()) {
            String sanitized = sanitizeSql(sql);
            log.debug("SQL: {}", sanitized);
        }
        
        // Return original SQL unmodified for execution
        return sql;
    }

    /**
     * Sanitizes SQL statements before logging to prevent exposure of sensitive data.
     * 
     * <p>This method applies several security measures:</p>
     * <ul>
     *   <li>Normalizes whitespace for cleaner log output (Java 21 optimized)</li>
     *   <li>Masks potential sensitive patterns (passwords, tokens, API keys)</li>
     *   <li>Truncates long SQL statements to prevent log flooding</li>
     * </ul>
     * 
     * @param sql the raw SQL statement
     * @return sanitized SQL statement safe for logging
     */
    String sanitizeSql(@NonNull String sql) {
        String sanitized = sql;
        
        // Normalize whitespace for cleaner logs
        sanitized = sanitized.replaceAll("\\s+", " ").trim();
        
        // Mask potential sensitive data patterns (case-insensitive)
        sanitized = sanitized.replaceAll("(?i)(password|token|secret|api[_-]?key)\\s*=\\s*'[^']*'", "$1='*****'");
        sanitized = sanitized.replaceAll("(?i)(password|token|secret|api[_-]?key)\\s*=\\s*\"[^\"]*\"", "$1=\"*****\"");
        
        // Truncate if too long
        if (sanitized.length() > MAX_SQL_LOG_LENGTH) {
            sanitized = sanitized.substring(0, MAX_SQL_LOG_LENGTH) + "...";
        }
        
        return sanitized;
    }
}



