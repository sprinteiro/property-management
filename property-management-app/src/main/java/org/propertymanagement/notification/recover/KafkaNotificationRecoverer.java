package org.propertymanagement.notification.recover;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.propertymanagement.notification.exception.FailedNotificationException;
import org.propertymanagement.notification.v1.NotificationRequest;
import org.propertymanagement.util.CorrelationIdLog;
import org.propertymanagement.util.CorrelationIdUtil;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.support.KafkaHeaders;

import java.nio.charset.StandardCharsets;

import static java.util.Objects.nonNull;
import static org.propertymanagement.associationmeeting.config.KafkaTopicsConfig.TOPIC_NOTIFICATION_REQUEST_DLT;
import static org.propertymanagement.util.KafkaUtil.longToBytes;

@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationRecoverer implements ConsumerRecordRecoverer {
    private final KafkaTemplate<String, GenericRecord> kafkaTemplate;
    private final CorrelationIdLog correlationIdLog;

    @Override
    public void accept(ConsumerRecord<?, ?> consumerRecord, Exception exception) {
        String correlationId = CorrelationIdUtil.correlationIdAsString(consumerRecord.headers().lastHeader(KafkaHeaders.CORRELATION_ID).value());
        correlationIdLog.execWithProvidedCorrelationId(correlationId, () -> {
            if (nonNull(exception) && ExceptionUtils.getRootCause(exception) instanceof FailedNotificationException) {
                log.info("About to send to {} from {}", TOPIC_NOTIFICATION_REQUEST_DLT, consumerRecord.topic());
                ProducerRecord<String, GenericRecord> record = newProducerRecordWithCorrelationIdHeader(TOPIC_NOTIFICATION_REQUEST_DLT, (ConsumerRecord<String, NotificationRequest>) consumerRecord);
                kafkaTemplate.send(record);
                log.info("Successfully sent to {} from {}", TOPIC_NOTIFICATION_REQUEST_DLT, consumerRecord.topic());
            }
        });
    }

    private ProducerRecord<String, GenericRecord> newProducerRecordWithCorrelationIdHeader(String topicName, ConsumerRecord<String, NotificationRequest> consumerRecord) {
        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(topicName, consumerRecord.value());
        record.headers()
                .add(KafkaHeaders.CORRELATION_ID, consumerRecord.headers().lastHeader(KafkaHeaders.CORRELATION_ID).value())
                .add(KafkaHeaders.DLT_ORIGINAL_TOPIC, consumerRecord.topic().getBytes(StandardCharsets.UTF_8))
                .add(KafkaHeaders.ORIGINAL_TIMESTAMP, longToBytes(consumerRecord.timestamp()))
        ;
        return record;
    }

}
