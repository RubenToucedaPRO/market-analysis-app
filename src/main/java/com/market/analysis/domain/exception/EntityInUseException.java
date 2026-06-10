package com.market.analysis.domain.exception;

/**
 * Domain exception thrown when an entity cannot be deleted because it has
 * associated dependencies (referential integrity violation).
 *
 * <p>The domain throws this exception with an {@code errorCode} (unique key)
 * and optional parameters. The {@code GlobalExceptionHandler} in Presentation
 * resolves the final message using {@code MessageSource}.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 *   throw new EntityInUseException("entity.in_use", ruleCode);
 * </pre>
 */
public class EntityInUseException extends RuntimeException {

    private final String errorCode;
    private final transient Object[] params;

    public EntityInUseException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = new Object[0];
    }

    public EntityInUseException(String errorCode, Object... params) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = params;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getParams() {
        return params;
    }
}
