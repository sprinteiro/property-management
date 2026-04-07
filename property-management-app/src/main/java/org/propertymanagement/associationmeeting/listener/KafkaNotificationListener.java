package org.propertymanagement.associationmeeting.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.propertymanagement.domain.*;
import org.propertymanagement.domain.notification.Meeting;
import org.propertymanagement.domain.notification.NotificationDelivery;
import org.propertymanagement.domain.notification.NotificationRequest.NotificationChannel;
import org.propertymanagement.domain.notification.Recipient;
import org.propertymanagement.notification.NotificationManager;
import org.propertymanagement.notification.v1.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import static java.util.Objects.isNull;
import static org.propertymanagement.associationmeeting.notifier.KafkaMeetingNotifier.MEETING_NOTIFICATION;
import static org.propertymanagement.associationmeeting.notifier.KafkaMeetingNotifier.TOPIC_NOTIFICATION_REQUEST;

public class KafkaNotificationListener {
    private static final Logger log = LoggerFactory.getLogger(KafkaNotificationListener.class);

    private final NotificationManager notificationManager;

    public KafkaNotificationListener(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }


    @KafkaListener(topics = { TOPIC_NOTIFICATION_REQUEST }, groupId = "${kafka.topic.group-id.meeting}")
    public void receiveNotificationRequest(ConsumerRecord<String, NotificationRequest> record) {
        NotificationRequest notification = record.value();
        log.info("Received record={} Key={}", notification, record.key());
        // Fetch recipients's phoneNumber and e-mail
        // Send SMS or e-mail
        if (!MEETING_NOTIFICATION.equalsIgnoreCase(notification.getNotificationType())) {
            // Silently skip processing as not a meeting notification
            return;
        }

        process(notification);
    }

    private void process(NotificationRequest notificationRequest) {
        if (isNull(notificationRequest) || isNull(notificationRequest.getRecipient())) {
            log.warn("Discarding request notification.");
            return;
        }

        Meeting meeting = new Meeting(
                new MeetingDate(notificationRequest.getDate()),
                new MeetingTime(notificationRequest.getTime()),
                new MeetingSubject("Community association meeting", "Meeting description here")
        );

        org.propertymanagement.notification.v1.Recipient recipient = notificationRequest.getRecipient();
        NotificationChannel channelType = NotificationChannel.getChannelFrom(recipient.getChannel());
        var notification = new NotificationDelivery<Meeting>(
                NotificationDelivery.NotificationType.MEETING,
                new CommunityId(notificationRequest.getCommunityId()),
                new Recipient(new NeighbourgId(recipient.getId()),
                        channelType,
                        channelType == NotificationChannel.EMAIL ? new Email(recipient.getAddress()) : new PhoneNumber(recipient.getAddress()),
                        new Name(recipient.getName())
                ),
                meeting);

        notificationManager.sendNotification(notification);
    }

}
