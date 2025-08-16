package org.propertymanagement.domain.search;

import java.util.List;

public record OrderedBy(List<OrderField> fields) {
    public static OrderedBy of(List<OrderField> fields) {
        return new OrderedBy(fields);
    }

    public record OrderField(String name, OrderedBy.SearchOrder orderBy) {
        public static OrderField of(String name) {
            return new OrderField(name, OrderedBy.SearchOrder.NONE);
        }

        public static OrderField of(String name, OrderedBy.SearchOrder orderBy) {
            return new OrderField(name, orderBy);
        }
    }

    public enum SearchOrder {
        ASC,
        DESC,
        NONE,
    }
}