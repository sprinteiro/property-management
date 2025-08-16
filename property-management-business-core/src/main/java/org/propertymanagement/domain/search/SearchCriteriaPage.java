package org.propertymanagement.domain.search;

public record SearchCriteriaPage(
    Integer pageSize,
    Integer pageNumber
) {
    public static SearchCriteriaPage of(Integer pageSize, Integer pageNumber) {
        return new SearchCriteriaPage(pageSize, pageNumber);
    }
}
