package org.propertymanagement.notification.notifier;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.propertymanagement.associationmeeting.notification.FailedNotification;
import org.propertymanagement.domain.notification.Meeting;
import org.propertymanagement.domain.notification.NotificationDelivery;
import org.propertymanagement.notification.v1.NotificationRequest;
import org.propertymanagement.notification.v1.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import static org.propertymanagement.associationmeeting.config.KafkaTopicsConfig.TOPIC_NOTIFICATION_REQUEST_DLT;

public class KafkaFailedNotificationNotifier implements FailedNotification {
    private static final Logger log = LoggerFactory.getLogger(KafkaFailedNotificationNotifier.class);
    private final KafkaTemplate<String, GenericRecord> kafkaTemplate;

    public KafkaFailedNotificationNotifier(KafkaTemplate<String, GenericRecord> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    @Override
    public void notifyFailedMeetingNotification(NotificationDelivery notification) {
        NotificationRequest avroNotificationRequest = toAvroNotificationRequest(notification);
        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(TOPIC_NOTIFICATION_REQUEST_DLT, avroNotificationRequest);
        log.info("Publishing failed meeting notification. Topic={} Message={}", record.topic(), record.value());
        kafkaTemplate.send(record);
        log.info("Produced event to topic {} Message={}", record.topic(), record.value());
    }

    private NotificationRequest toAvroNotificationRequest(NotificationDelivery notification) {
        Meeting meeting = (Meeting) notification.details();

        var recipient = notification.recipient();
        return new NotificationRequest(
                org.propertymanagement.domain.notification.NotificationRequest.NotificationType.MEETING.name(),
                new Recipient(
                        recipient.id().value(),
                        recipient.channel().name(),
                        recipient.address().getAddress(),
                        recipient.name().value()
                ),
                meeting.date().value(),
                meeting.time().value(),
                notification.communityId().value()
        );
    }

}
