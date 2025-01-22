package org.propertymanagement.notification;

import org.propertymanagement.associationmeeting.MeetingScheduler;
import org.propertymanagement.notification.web.controller.NotificationController;
import org.propertymanagement.util.CorrelationIdLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

public class WebNotificationMeetingConfig {
    @ConditionalOnProperty(name = "test.endpoints", havingValue = "on")
    @Bean
    public NotificationController notificationController(CorrelationIdLog correlationIdLog, MeetingScheduler meetingScheduler) {
        return new NotificationController(correlationIdLog, meetingScheduler);
    }

}
