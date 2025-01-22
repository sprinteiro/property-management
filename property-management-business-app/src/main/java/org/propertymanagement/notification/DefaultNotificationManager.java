package org.propertymanagement.notification;

import org.propertymanagement.associationmeeting.notification.FailedNotification;
import org.propertymanagement.domain.notification.Meeting;
import org.propertymanagement.domain.notification.NotificationDelivery;
import org.propertymanagement.domain.notification.NotificationRequest;
import org.propertymanagement.notification.exception.FailedNotificationException;
import org.propertymanagement.notification.exception.NotificationException;
import org.propertymanagement.util.CorrelationIdLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static org.propertymanagement.util.CorrelationIdUtil.correlationIdAsString;

public class DefaultNotificationManager implements NotificationManager {
    private static final Logger log = LoggerFactory.getLogger(DefaultNotificationManager.class);

    private final EmailNotificationSender emailNotificationSender;
    private final SmsNotificationSender smsNotificationSender;
    private final Executor notificationExecutor;
    private final CorrelationIdLog correlationIdLog;
    private final FailedNotification kafkaFailedNotificationNotifier;


    public DefaultNotificationManager(
            EmailNotificationSender emailNotificationSender,
            SmsNotificationSender smsNotificationSender,
            Executor notificationExecutor,
            CorrelationIdLog correlationIdLog, FailedNotification failedNotificationManager
    ) {
        this.emailNotificationSender = emailNotificationSender;
        this.smsNotificationSender = smsNotificationSender;
        this.notificationExecutor = notificationExecutor;
        this.correlationIdLog = correlationIdLog;
        this.kafkaFailedNotificationNotifier = failedNotificationManager;
    }

    @Override
    public void sendNotification(NotificationDelivery<Meeting> notification) {
        NotificationRequest.NotificationChannel notificationChannel = notification.recipient().channel();
        if (notificationChannel == NotificationRequest.NotificationChannel.SMS) {
            send(notification, smsNotificationSender::sendNotification);
        } else if (notificationChannel == NotificationRequest.NotificationChannel.EMAIL) {
            send(notification, emailNotificationSender::sendNotification);
        }
    }

    private void send(NotificationDelivery notificationRequest, Consumer<NotificationDelivery> sender) {
        correlationIdLog.execWithProvidedCorrelationId(correlationIdAsString(notificationRequest.correlationId()), () -> {
            Map<String, String> copyOfContextMap = correlationIdLog.getCopyOfContextMap();
            CompletableFuture.runAsync(() -> {
                        correlationIdLog.setContextMap(copyOfContextMap);
                        log.info("About to send notifications to {}", notificationRequest.recipient());
                        sender.accept(notificationRequest);
                    },
                    notificationExecutor
            ).exceptionally(throwable -> {
                Throwable cause = throwable.getCause();
                if (cause instanceof FailedNotificationException exception) {
                    log.info("DLT retry. RecipientId={} Reason={} Notification=[{}]", exception.getRecipientId(), exception.getReason(), notificationRequest);
                    kafkaFailedNotificationNotifier.notifyFailedMeetingNotification(notificationRequest);
                } else if (cause instanceof NotificationException exception) {
                    log.info("DLT. Reason={} Notification=[{}]", exception.getMessage(), notificationRequest);
                    kafkaFailedNotificationNotifier.notifyFailedMeetingNotification(notificationRequest);
                }
                return null;
            });
        });
    }

}
