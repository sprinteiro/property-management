package org.propertymanagement.domain.search;

import java.util.List;
import java.util.Objects;

/**
 * Search criteria to allow search data by a type, allowing filtering by fields and ordering by fields.
 * @param orderBy order for the search
 * @param filteredBy filters to apply for the search
 * @param clazz data type/class
 * @param <T> type of data
 */
public record SearchCriteria<T>(
        OrderedBy orderBy,
        FilteredBy filteredBy,
        Class<T> clazz,
        SearchCriteriaPage page
) {
    public SearchCriteria(Class<T> clazz) {
        this(
                null,
                null,
                clazz,
                new SearchCriteriaPage(1, 0)
        );
    }

    public SearchCriteria(Class<T> clazz, Integer pageNumber) {
        this(
                null,
                null,
                clazz,
                new SearchCriteriaPage(1, Objects.requireNonNull(pageNumber, "pageNumber must not be null"))
        );
    }

    public List<SearchFilter> filters() {
        if (Objects.isNull(filteredBy)) {
            return List.of();
        }
        return filteredBy.fields();
    }
}
