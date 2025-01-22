package org.propertymanagement.notification.sms;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.propertymanagement.domain.notification.Meeting;
import org.propertymanagement.domain.notification.NotificationDelivery;
import org.propertymanagement.domain.notification.Recipient;
import org.propertymanagement.notification.SmsNotificationSender;
import org.propertymanagement.notification.exception.NotificationException;
import org.springframework.util.StringUtils;

import static java.util.Objects.isNull;

@Slf4j
@RequiredArgsConstructor
public class SmsTwilioNotificationSender implements SmsNotificationSender {
    private final String accountId;
    private final String authToken;


    @Override
    public boolean sendNotification(NotificationDelivery notificationRequest) {
        Recipient recipient = notificationRequest.recipient();
        if (isNull(recipient) || isNull(recipient.address()) || !StringUtils.hasText(recipient.address().getAddress())) {
            return false;
        }

        StringBuilder text = new StringBuilder();
        Meeting details = (Meeting) notificationRequest.details();
        text.append(details.meetingSubject().description())
                .append("\n").append("Date:").append(details.date().value())
                .append("\n").append("Time:").append(details.time().value());
        return sendSms(recipient, text.toString(), notificationRequest.correlationId());
    }

    private boolean sendSms(Recipient recipient, String text, byte[] correlationId) {
        Message message;
        try {
            log.info("Sending SMS to {}. NeighbourId={} CorrelationId={}", recipient.address().getAddress(), recipient.id(), correlationId);

            message = Message
                    .creator(new com.twilio.type.PhoneNumber(recipient.address().getAddress()),
                            new com.twilio.type.PhoneNumber("+17854536162"),
                            text
                    )
                    .create();
            log.info("Sent via Twilio Status={} Text={}", message.getStatus(), message.getBody());
            return true;
        } catch (Exception e) {
            throw new NotificationException(recipient.id().value(), e.getMessage(), null, e);
        }
    }

    @PostConstruct
    public void setup() {
        log.info("Initializing Twilio for SID={}", accountId);
        Twilio.init(accountId, authToken);
    }

}

