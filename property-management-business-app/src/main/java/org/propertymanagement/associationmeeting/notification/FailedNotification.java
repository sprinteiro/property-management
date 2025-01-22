package org.propertymanagement.associationmeeting.notification;

import org.propertymanagement.domain.notification.NotificationDelivery;

public interface FailedNotification {
    void notifyFailedMeetingNotification(NotificationDelivery notification);
}
