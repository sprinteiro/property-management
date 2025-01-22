package org.propertymanagement.domain;

public record TrackerId(java.util.UUID value) {
    public String toString() {
        return value().toString();
    }
}
