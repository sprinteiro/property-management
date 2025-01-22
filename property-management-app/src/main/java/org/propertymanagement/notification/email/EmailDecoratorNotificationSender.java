package org.propertymanagement.notification.email;

import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.propertymanagement.domain.notification.NotificationDelivery;
import org.propertymanagement.domain.notification.Recipient;
import org.propertymanagement.notification.EmailNotificationSender;
import org.propertymanagement.notification.NotificationSender;
import org.propertymanagement.notification.exception.FailedNotificationException;
import org.propertymanagement.notification.exception.NotificationException;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.Supplier;

@RequiredArgsConstructor
@Slf4j
public class EmailDecoratorNotificationSender implements EmailNotificationSender {
    private final NotificationSender notificationSender;
    @Value("${email.retries:false}")
    private boolean retryEnabled;
    private final Retry retryNotification;

    @Override
    public boolean sendNotification(NotificationDelivery notificationRequest) {
        if (retryEnabled) {
            sendRetriableEmail(notificationRequest);
        } else {
            notificationSender.sendNotification(notificationRequest);
        }
        return true;
    }

    private void sendRetriableEmail(NotificationDelivery notificationRequest) {
        Supplier<Boolean> sender = Retry.decorateSupplier(
                retryNotification,
                () -> notificationSender.sendNotification(notificationRequest)
        );

        try {
            Boolean result = sender.get();
            Recipient recipient = notificationRequest.recipient();
            log.info("E-mail send result={} for email={}", result, recipient.address().getAddress());
        } catch (NotificationException e) {
            // Handle exception after retries are exhausted
            // Raise this exception to be DLT
            Recipient recipient = notificationRequest.recipient();
            throw new FailedNotificationException(e.getMessage(), null, "Exhausted retries for e-mail send" + e.getMessage(), recipient.id().value(), e);
        }
    }

}
