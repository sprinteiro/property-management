package org.propertymanagement.domain.search;

public record SearchFilter(FieldName field, FieldValue value, FilterOperator operator) {

    public enum FilterOperator {
                AND,
                OR,
    }
}
