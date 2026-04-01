package org.propertymanagement.notification.email;

import org.propertymanagement.domain.notification.NotificationDelivery;
import org.propertymanagement.domain.notification.Recipient;
import org.propertymanagement.notification.EmailNotificationSender;
import org.propertymanagement.notification.exception.NotificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

import static java.util.Objects.isNull;
import static org.propertymanagement.util.CorrelationIdUtil.correlationIdAsString;

public class EmailStubNotificationSender implements EmailNotificationSender {
    private static final Logger log = LoggerFactory.getLogger(EmailStubNotificationSender.class);

    private static final Set<Long> VALID_RECIPIENTS = Set.of(1L, 2L);
    @Value("${test.notification.stub.email.error.enabled:false}")
    private boolean enableErrors;


    @Override
    public boolean sendNotification(NotificationDelivery notificationRequest) {
        Recipient recipient = notificationRequest.recipient();
        log.info("----------> Recipient to notify={}", recipient);
        if (isNull(recipient)) {
            log.warn("----------> No recipients to notify");
            return false;
        }

        if (enableErrors && !VALID_RECIPIENTS.contains(recipient.id().value())) {
            // Should retry when retries is enabled for invalid recipients
            throw new NotificationException("Fake e-mail exception for " + recipient.address().getAddress());
        }

        log.info("----------> DISABLED. Sending {} to {}. NeighbourId={} CorrelationId={}", recipient.channel(), recipient.address(), recipient.id(), correlationIdAsString(notificationRequest.correlationId()));
        return true;
    }
}
