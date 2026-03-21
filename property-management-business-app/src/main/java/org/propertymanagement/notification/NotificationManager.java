package org.propertymanagement.notification;

import org.propertymanagement.domain.Email;
import org.propertymanagement.domain.MeetingNotificationRequest;
import org.propertymanagement.domain.Participant;
import org.propertymanagement.domain.PhoneNumber;
import org.propertymanagement.domain.notification.Meeting;
import org.propertymanagement.domain.notification.NotificationDelivery;
import org.propertymanagement.domain.notification.Recipient;

import java.util.Set;

import static java.util.Objects.isNull;
import static org.propertymanagement.notification.NotificationManager.InvalidRecipientNotification.Reason.NO_PHONE_NUMBER_AND_NO_EMAIL;


public interface NotificationManager {
    void sendNotification(NotificationDelivery<Meeting> notification);

    static Set<InvalidRecipientNotification> checkAllCanBeNotified(MeetingNotificationRequest meetingNotificationRequest) {
        Participant participant = meetingNotificationRequest.recipient();
        PhoneNumber phoneNumber = participant.phoneNumber();
        Email email = participant.email();
        boolean isMissingPhoneNumber = isNull(phoneNumber) || isNull(phoneNumber.value()) || phoneNumber.value().isBlank();
        boolean isMissingEmail = isNull(email) || isNull(email.value()) || email.value().isBlank();

        if (isMissingPhoneNumber && isMissingEmail) {
            Recipient recipient = new Recipient(participant.id(), null, null, participant.name());
            return Set.of(new InvalidRecipientNotification(recipient, NO_PHONE_NUMBER_AND_NO_EMAIL));
        }
        return Set.of();
    }

    record InvalidRecipientNotification(Recipient recipient, Reason reason) {
        public enum Reason {
            NO_PHONE_NUMBER_AND_NO_EMAIL
        }
    }
}
