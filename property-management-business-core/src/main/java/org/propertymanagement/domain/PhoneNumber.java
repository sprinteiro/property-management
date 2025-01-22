package org.propertymanagement.domain;

import org.propertymanagement.domain.notification.RecipientAddress;

public record PhoneNumber(String value) implements RecipientAddress {
    @Override
    public String getAddress() {
        return value();
    }
}
