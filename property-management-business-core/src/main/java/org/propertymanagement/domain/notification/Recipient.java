package org.propertymanagement.domain.notification;

import org.propertymanagement.domain.Name;
import org.propertymanagement.domain.NeighbourgId;
import org.propertymanagement.domain.notification.NotificationRequest.NotificationChannel;

import java.util.Arrays;
import java.util.Objects;

public record Recipient(
        NeighbourgId id,
        NotificationChannel channel,
        RecipientAddress address,
        Name name
) {

    public enum Role {
        ADMINISTRATOR,
        PRESIDENT,
        VICEPRESIDENT,
        COMMUNITY_MEMBER;

        public static Role getRoleFrom(String from) {
            return Arrays.stream(Role.values())
                    .filter(type -> type.name().equalsIgnoreCase(from))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown recipients role: " + from));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Recipient that = (Recipient) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
