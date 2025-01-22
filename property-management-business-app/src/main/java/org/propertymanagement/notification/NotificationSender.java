package org.propertymanagement.notification;

import org.propertymanagement.domain.notification.NotificationDelivery;

public interface NotificationSender<T> {
    boolean sendNotification(NotificationDelivery<T> notificationRequest);
}
