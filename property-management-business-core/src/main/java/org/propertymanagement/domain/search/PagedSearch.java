package org.propertymanagement.domain.search;

import java.util.List;

/**
 * Offset-based pagination
 * 
 * @param items
 * @param totalElements
 * @param totalPages
 * @param pageNumber
 * @param <T>
 */
public record PagedSearch<T>(
    List<T> items,
    Long totalElements,
    Integer totalPages,
    Integer pageNumber
) {
}
