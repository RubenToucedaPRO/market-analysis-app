package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

import com.market.analysis.domain.exception.DomainErrorCodes;

/**
 * Immutable value object representing a valid entry price for a trade.
 *
 * <p>The entry price must be strictly greater than zero. Use the static
 * factory method {@link #of(BigDecimal)} to create instances.</p>
 *
 * @param value the entry price as a {@link BigDecimal}
 */
public record EntryPrice(BigDecimal value) {

    public static final String FIELD_ENTRY_PRICE = "Entry price";

    public EntryPrice {
        Objects.requireNonNull(value, DomainErrorCodes.ENTRY_PRICE_NULL);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Entry price must be greater than zero");
        }
    }

    /**
     * Static factory method for creating an {@code EntryPrice}.
     *
     * @param value the entry price as a {@link BigDecimal}
     * @return a validated {@code EntryPrice} instance
     */
    public static EntryPrice of(BigDecimal value) {
        return new EntryPrice(value);
    }
}
