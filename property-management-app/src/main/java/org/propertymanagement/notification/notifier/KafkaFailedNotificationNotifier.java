package org.propertymanagement.notification.notifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.propertymanagement.associationmeeting.notification.FailedNotification;
import org.propertymanagement.domain.notification.Meeting;
import org.propertymanagement.domain.notification.NotificationDelivery;
import org.propertymanagement.notification.v1.NotificationRequest;
import org.propertymanagement.notification.v1.Recipient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;

import static org.propertymanagement.associationmeeting.config.KafkaTopicsConfig.TOPIC_NOTIFICATION_REQUEST_DLT;
import static org.propertymanagement.util.KafkaHeadersUtil.correlationIdAsString;

@Slf4j
@RequiredArgsConstructor
public class KafkaFailedNotificationNotifier implements FailedNotification {
    private final KafkaTemplate<String, GenericRecord> kafkaTemplate;


    @Override
    public void notifyFailedMeetingNotification(NotificationDelivery notification) {
        NotificationRequest avroNotificationRequest = toAvroNotificationRequest(notification);
        ProducerRecord<String, GenericRecord> record = newProducerRecordWithCorrelationIdHeader(TOPIC_NOTIFICATION_REQUEST_DLT, avroNotificationRequest, notification.correlationId());
        log.info("Publishing failed meeting notification. CorrelationId={} Topic={} Message={}", correlationIdAsString(notification.correlationId()), record.topic(), record.value());
        kafkaTemplate.send(record);
        log.info("Produced event to topic {} CorrelationId={} Message={}", record.topic(), correlationIdAsString(record), record.value());
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

    private ProducerRecord<String, GenericRecord> newProducerRecordWithCorrelationIdHeader(String topicName, NotificationRequest notificationRequest, byte[] correlationId) {
        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(topicName, notificationRequest);
        record.headers()
                .add(KafkaHeaders.CORRELATION_ID, correlationId)
        ;
        return record;
    }

}
