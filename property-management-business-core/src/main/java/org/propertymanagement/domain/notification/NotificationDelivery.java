package org.propertymanagement.domain.notification;

import org.propertymanagement.domain.CommunityId;

import java.util.Objects;

public record NotificationDelivery<T>(
        NotificationType type,
        CommunityId communityId,
        Recipient recipient,
        T details) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationDelivery<?> that = (NotificationDelivery<?>) o;
        return type == that.type &&
                Objects.equals(communityId, that.communityId) &&
                Objects.equals(recipient, that.recipient) &&
                Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, communityId, recipient, details);
    }

    @Override
    public String toString() {
        return "NotificationDelivery[" +
                "type=" + type +
                ", communityId=" + communityId +
                ", recipient=" + recipient +
                ", details=" + details +
                ']';
    }

    public enum NotificationType {
        MEETING,
        UNKNOWN
    }
}
