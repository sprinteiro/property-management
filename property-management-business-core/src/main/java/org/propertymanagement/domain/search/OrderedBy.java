package org.propertymanagement.domain.search;

import java.util.List;

public record OrderedBy(SearchOrder order, List<FieldName> fields) {

    public OrderedBy(SearchOrder order, List<FieldName> fields) {
        if (order != SearchOrder.NONE && fields.isEmpty()) {
            throw new IllegalArgumentException("Fields must be provided when order is not NONE");
        }
        this.order = order;
        this.fields = fields;
    }

    public enum SearchOrder {
        ASC,
        DESC,
        NONE,
    }

    public boolean none() {
        return order == SearchOrder.NONE;
    }
}



