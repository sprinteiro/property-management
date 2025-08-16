package org.propertymanagement.domain.search;


public record SearchCriteriaPage(
    Integer pageSize,
    Integer pageNumber
) {
}
