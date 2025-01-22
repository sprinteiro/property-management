package org.propertymanagement.notification.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.propertymanagement.domain.notification.Meeting;
import org.propertymanagement.domain.notification.NotificationDelivery;
import org.propertymanagement.notification.EmailNotificationSender;
import org.propertymanagement.notification.exception.NotificationException;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;

import static java.util.Objects.isNull;
import static org.propertymanagement.util.CorrelationIdUtil.correlationIdAsString;

@RequiredArgsConstructor
@Slf4j
public class JavaEmailNotificationSender implements EmailNotificationSender {
    private final JavaMailSender emailSender;
    private final SimpleMailMessage emailTemplate;


    @Override
    public boolean sendNotification(NotificationDelivery notificationRequest) {
        var recipient = notificationRequest.recipient();
        try {
            if (isNull(recipient) || isNull(recipient.address()) || !StringUtils.hasText(recipient.address().getAddress())) {
                // Skip notification with no e-mail address
                return false;
            }

            Meeting meeting = (Meeting) notificationRequest.details();
            MailMessage emailToSent = new SimpleMailMessage();
            emailTemplate.copyTo(emailToSent);
            emailToSent.setTo(recipient.address().getAddress());
            emailToSent.setSubject(meeting.meetingSubject().subject());
            String emailBody = ((SimpleMailMessage) emailToSent).getText()
                    .replace("{recipientName}", recipient.name().value())
                    .replace("{date}", meeting.date().value())
                    .replace("{time}", meeting.time().value())
                    .replace("{description}", meeting.meetingSubject().description());
            emailToSent.setText(emailBody);

            log.info("Sending e-mail to {}. NeighbourId={} CorrelationId={}", recipient.address().getAddress(), recipient.id(), correlationIdAsString(notificationRequest.correlationId()));
            emailSender.send((SimpleMailMessage) emailToSent);
            log.info("Sent via Java e-mail E-mail={}", ((SimpleMailMessage) emailToSent).getTo()[0]);
            return true;
        } catch (Exception e) {
            throw new NotificationException(recipient.id().value(), e.getMessage(), null, e);
        }
    }
}
