package org.propertymanagement.domain;

import java.util.Objects;

public record NeighbourgId(Long value) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NeighbourgId that = (NeighbourgId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
