package org.propertymanagement.domain.search;

public record SearchFilter(FieldName field, FieldValue<Object> value, Hint hint) {
    public static SearchFilter of(String fieldName, Object fieldValue, Hint hint) {
        return new SearchFilter(new FieldName(fieldName), new FieldValue<>(fieldValue), hint);
    }

    public static SearchFilter of(String fieldName, Object fieldValue) {
        return of(fieldName, fieldValue, Hint.NONE);
    }

    public enum Hint {
        EXACT_MATCH,
        LIKE_MATCH,
        NONE,
    }
}
