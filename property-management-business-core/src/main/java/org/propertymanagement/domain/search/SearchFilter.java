package org.propertymanagement.domain.search;

public record SearchFilter(FieldName field, FieldValue<String> value) {

    public static SearchFilter of(String fieldName, String fieldValue) {
        return new SearchFilter(new FieldName(fieldName), new FieldValue<>(fieldValue));
    }
}
