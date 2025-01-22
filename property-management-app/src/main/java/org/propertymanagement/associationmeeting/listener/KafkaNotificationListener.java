package org.propertymanagement.associationmeeting.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.propertymanagement.domain.*;
import org.propertymanagement.domain.notification.Meeting;
import org.propertymanagement.domain.notification.NotificationDelivery;
import org.propertymanagement.domain.notification.NotificationRequest.NotificationChannel;
import org.propertymanagement.domain.notification.Recipient;
import org.propertymanagement.notification.NotificationManager;
import org.propertymanagement.notification.v1.NotificationRequest;
import org.propertymanagement.util.CorrelationIdLog;
import org.propertymanagement.util.CorrelationIdUtil;
import org.propertymanagement.util.KafkaHeadersUtil;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import static java.util.Objects.isNull;
import static org.propertymanagement.associationmeeting.notifier.KafkaMeetingNotifier.MEETING_NOTIFICATION;
import static org.propertymanagement.associationmeeting.notifier.KafkaMeetingNotifier.TOPIC_NOTIFICATION_REQUEST;

@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationListener {
    private final NotificationManager notificationManager;
    private final CorrelationIdLog correlationIdLog;


    @KafkaListener(topics = { TOPIC_NOTIFICATION_REQUEST }, groupId = "${kafka.topic.group-id.meeting}")
    public void receiveNotificationRequest(ConsumerRecord<String, NotificationRequest> record, @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {
        correlationIdLog.execWithProvidedCorrelationId(CorrelationIdUtil.correlationIdAsString(correlationId), () -> {
            NotificationRequest notification = record.value();
            log.info("Received record={} Key={} CorrelationId={}", notification, record.key(), KafkaHeadersUtil.correlationIdAsString(correlationId));
            // Fetch recipients's phoneNumber and e-mail
            // Send SMS or e-mail
            if (!MEETING_NOTIFICATION.equalsIgnoreCase(notification.getNotificationType())) {
                // Silently skip processing as not a meeting notification
                return;
            }

            process(notification, correlationId);
        });
    }

    private void process(NotificationRequest notificationRequest, byte[] correlationId) {
        if (isNull(notificationRequest) || isNull(notificationRequest.getRecipient())) {
            log.warn("Discarding request notification.");
            return;
        }

        Meeting meeting = new Meeting(
                new MeetingDate(notificationRequest.getDate()),
                new MeetingTime(notificationRequest.getTime()),
                new MeetingSubject("Community association meeting", "Meeting description here"),
                correlationId
        );

        org.propertymanagement.notification.v1.Recipient recipient = notificationRequest.getRecipient();
        NotificationChannel channelType = NotificationChannel.getChannelFrom(recipient.getChannel());
        var notification = new NotificationDelivery<Meeting>(
                NotificationDelivery.NotificationType.MEETING,
                correlationId,
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