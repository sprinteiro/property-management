package org.propertymanagement.domain.search;


import org.propertymanagement.domain.CommunityInfo;

import java.util.List;

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
        this(new OrderedBy(OrderedBy.SearchOrder.NONE, List.of()), new FilteredBy(), clazz, null);
    }

    public List<SearchFilter> filters() {
        return filteredBy.fields();
    }
}
