package org.propertymanagement.domain.notification;

import org.propertymanagement.domain.CommunityId;

import java.util.Arrays;
import java.util.List;

public record NotificationRequest<T>(
        NotificationType type,
        byte[] correlationId,
        CommunityId communityId,
        List<Recipient> recipients,
        T details) {

    public enum NotificationType {
        MEETING,
        UNKNOWN
    }

    public enum NotificationChannel {
        SMS,
        EMAIL;

        public static NotificationChannel getChannelFrom(String from) {
            return Arrays.stream(NotificationChannel.values())
                    .filter(type -> type.name().equalsIgnoreCase(from))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown channel: " + from));
        }
    }
}
