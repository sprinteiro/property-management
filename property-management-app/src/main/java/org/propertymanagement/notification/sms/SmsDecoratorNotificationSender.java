package org.propertymanagement.notification.sms;

import io.github.resilience4j.retry.Retry;
import org.propertymanagement.domain.notification.NotificationDelivery;
import org.propertymanagement.domain.notification.Recipient;
import org.propertymanagement.notification.NotificationSender;
import org.propertymanagement.notification.SmsNotificationSender;
import org.propertymanagement.notification.exception.FailedNotificationException;
import org.propertymanagement.notification.exception.NotificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.Supplier;

public class SmsDecoratorNotificationSender implements SmsNotificationSender {
    private static final Logger log = LoggerFactory.getLogger(SmsDecoratorNotificationSender.class);

    private final NotificationSender notificationSender;
    @Value("${sms.retries:false}")
    private boolean retryEnabled;
    private final Retry retryNotification;

    public SmsDecoratorNotificationSender(NotificationSender notificationSender, Retry retryNotification) {
        this.notificationSender = notificationSender;
        this.retryNotification = retryNotification;
    }

    @Override
    public boolean sendNotification(NotificationDelivery notification) {
        if (retryEnabled) {
            sendRetriableSms(notification);
        } else {
            notificationSender.sendNotification(notification);
        }
        return true;
    }

    private void sendRetriableSms(NotificationDelivery notificationRequest) {
        Supplier<Boolean> sender = Retry.decorateSupplier(
                retryNotification,
                () -> notificationSender.sendNotification(notificationRequest)
        );

        try {
            Boolean result = sender.get();
            Recipient recipient = notificationRequest.recipient();
            log.info("SMS send result={} for number={}", result, recipient.address().getAddress());
        } catch (NotificationException e) {
            // Handle exception after retries are exhausted
            // Raise this exception to be DLT
            Recipient recipient = notificationRequest.recipient();
            throw new FailedNotificationException(e.getMessage(), null, "Exhausted retries for SMS send" + e.getMessage(), recipient.id().value(), e);
        }
    }

}
