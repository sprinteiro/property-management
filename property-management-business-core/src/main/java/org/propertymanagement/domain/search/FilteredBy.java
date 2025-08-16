package org.propertymanagement.domain.search;

import java.util.List;

public record FilteredBy(List<SearchFilter> fields) {
    public FilteredBy() {
        this(List.of());
    }
}
