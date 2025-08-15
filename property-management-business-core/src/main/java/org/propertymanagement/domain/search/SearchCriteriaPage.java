package org.propertymanagement.domain.search;

import java.util.List;

public record SearchCriteriaPage(
    Integer pageSize,
    Integer pageNumber, // For offset-based pagination
    List<Object> seekAfter // For keyset-based pagination
) {
    // Convenience constructors for different pagination types
    public static SearchCriteriaPage of(int pageSize, int pageNumber) {
        return new SearchCriteriaPage(pageSize, pageNumber, null);
    }

    public static SearchCriteriaPage of(int pageSize, List<Object> seekAfter) {
        return new SearchCriteriaPage(pageSize, null, seekAfter);
    }

    public boolean isKeyset() {
        return seekAfter != null && !seekAfter.isEmpty();
    }

    public boolean isOffset() {
        return pageNumber != null;
    }
}
