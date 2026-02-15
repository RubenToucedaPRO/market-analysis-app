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

    @Override
    @NonNull
    public String inspect(@NonNull String sql) {
        if (log.isDebugEnabled()) {
            log.debug("SQL a ejecutar: {}", sanitizeSql(sql));
        }
        return sql;
    }

    public String sanitizeSql(@NonNull String sql) {
        // 1. Limpieza de espacios (Java 21 optimized)
        String sanitized = sql.replaceAll("\\s+", " ").trim();

        // 2. Ofuscación de seguridad (Clave para cumplir con la LOPD en el TFM)
        sanitized = sanitized.replaceAll("(?i)(password|token|secret|api[_-]?key)\\s*=\\s*['\"][^'\"]*['\"]",
                "$1='*****'");

        return sanitized.length() > MAX_SQL_LOG_LENGTH
                ? sanitized.substring(0, MAX_SQL_LOG_LENGTH) + "..."
                : sanitized;
    }
}
