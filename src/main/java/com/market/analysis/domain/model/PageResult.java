package com.market.analysis.domain.model;

import java.util.List;

/**
 * Technology-agnostic pagination result for the domain layer.
 * Wraps a page of items with metadata about total count, page number, and size.
 *
 * @param <T> the type of items in the page
 */
public record PageResult<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {

    public boolean hasNext() {
        return pageNumber < totalPages - 1;
    }

    public boolean hasPrevious() {
        return pageNumber > 0;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }
}
