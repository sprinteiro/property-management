package org.propertymanagement.associationmeeting.notifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TimeoutException;
import org.propertymanagement.associationmeeting.notification.MeetingNotification;
import org.propertymanagement.domain.MeetingInvite;
import org.propertymanagement.domain.Participant;
import org.propertymanagement.domain.ScheduledAssociationMeeting;
import org.propertymanagement.notification.v1.NotificationRequest;
import org.propertymanagement.notification.v1.Recipient;
import org.propertymanagement.util.CorrelationIdLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.nonNull;
import static org.propertymanagement.associationmeeting.config.KafkaTopicsConfig.TOPIC_MEETING_APPROVAL_REQUEST;
import static org.propertymanagement.associationmeeting.config.KafkaTopicsConfig.TOPIC_MEETING_REGISTRATION_REQUEST;
import static org.propertymanagement.domain.notification.NotificationRequest.NotificationChannel.SMS;
import static org.propertymanagement.util.KafkaHeadersUtil.correlationIdAsString;

@RequiredArgsConstructor
@Slf4j
public class KafkaMeetingNotifier implements MeetingNotification {
    public static final String TOPIC_NOTIFICATION_REQUEST = "notification-request";
    public static final String MEETING_NOTIFICATION = "MEETING_NOTIFICATION";
    private static final int MAX_RETRY_ATTEMPTS = 2;
    @Value("${kafka.retry.timeout:120000}")
    private int timeout;
    private final KafkaTemplate<String, GenericRecord> kafkaTemplate;
    private final Executor kafkaExecutor;
    private final CorrelationIdLog correlationIdLog;

    @Override
    public void notifyForCreation(MeetingInvite invite) {
        correlationIdLog.execWithProvidedCorrelationId(correlationIdAsString(invite.getCorrelationId()), () -> {
                    Map<String, String> copyOfContextMap = correlationIdLog.getCopyOfContextMap();
                    ProducerRecord<String, GenericRecord> record = newProducerRecordWithCorrelationIdHeader(TOPIC_MEETING_REGISTRATION_REQUEST, toAvroMeetingInvite(invite), invite.getCorrelationId());
                    CompletableFuture.supplyAsync(() -> {
                                correlationIdLog.setContextMap(copyOfContextMap);
                                log.info("Publishing invite for creation. CorrelationId={} Topic={} Message={}", correlationIdAsString(invite.getCorrelationId()), record.topic(), record.value());
                                return kafkaTemplate.send(record);
                            }, kafkaExecutor)
                            .handle((result, throwable) -> {
                                if (ExceptionUtils.getRootCause(throwable) instanceof TimeoutException) {
                                    RetryDetails retryResult = retrySendToKafka(record);
                                    log.info("Retry result - {} CorrelationId={}", retryResult, correlationIdAsString(invite.getCorrelationId()));
                                } else if (nonNull(result)) {
                                    log.info("Produced event to topic {} CorrelationId={} Message={}", record.topic(), correlationIdAsString(record), record.value());
                                }
                                return result;
                            });
                }
        );
    }

    @Override
    public void notifyForApproval(MeetingInvite invite) {
        correlationIdLog.execWithProvidedCorrelationId(correlationIdAsString(invite.getCorrelationId()), () -> {
                    Map<String, String> copyOfContextMap = correlationIdLog.getCopyOfContextMap();
                    ProducerRecord<String, GenericRecord> record = newProducerRecordWithCorrelationIdHeader(TOPIC_MEETING_APPROVAL_REQUEST, toAvroMeetingInvite(invite), invite.getCorrelationId());
                    CompletableFuture.supplyAsync(() -> {
                                correlationIdLog.setContextMap(copyOfContextMap);
                                log.info("Publishing invite for approval. CorrelationId={} Topic={} Message={}", correlationIdAsString(invite.getCorrelationId()), record.topic(), record.value());
                                return kafkaTemplate.send(record);
                            }, kafkaExecutor)
                            .handle((result, throwable) -> {
                                if (ExceptionUtils.getRootCause(throwable) instanceof TimeoutException) {
                                    RetryDetails retryResult = retrySendToKafka(record);
                                    log.info("Retry result - {} CorrelationId={}", retryResult, correlationIdAsString(invite.getCorrelationId()));
                                } else if (nonNull(result)) {
                                    log.info("Produced event to topic {} CorrelationId={} Message={}", record.topic(), correlationIdAsString(record), record.value());
                                }
                                return result;
                            });
                }
        );
    }

    @Override
    public void notifyMeetingToParticipants(ScheduledAssociationMeeting scheduledMeeting) {
        correlationIdLog.execWithProvidedCorrelationId(correlationIdAsString(scheduledMeeting.correlationId()), () -> {
            Map<String, String> copyOfContextMap = correlationIdLog.getCopyOfContextMap();
            CompletableFuture.supplyAsync(() -> {
                        correlationIdLog.setContextMap(copyOfContextMap);
                        Collection<ProducerRecord<String, GenericRecord>> records = newProducerNotificationRequestRecordsWithCorrelationIdHeader(TOPIC_NOTIFICATION_REQUEST, scheduledMeeting);

                        records.forEach(record -> {
                            try {
                                log.info("Publishing notification request to notify recipients. CorrelationId={} Topic={} Message={}", correlationIdAsString(scheduledMeeting.correlationId()), record.topic(), record.value());
                                kafkaTemplate.send(record);
                                log.info("Produced event to topic {} CorrelationId={} Message={}", record.topic(), correlationIdAsString(record), record.value());
                            } catch (Exception e) {
                                if (ExceptionUtils.getRootCause(e) instanceof TimeoutException) {
                                    RetryDetails retryResult = retrySendToKafka(record);
                                    log.info("Retry result - {} CorrelationId={}", retryResult, correlationIdAsString(scheduledMeeting.correlationId()));
                                } else {
                                    throw e;
                                }
                            }
                        });
                        return null;
                    }, kafkaExecutor)
                    .handle((result, throwable) -> {
                        if (nonNull(throwable)) {
                            log.error("Notification error {}", throwable.getMessage(), throwable);
                        }
                        return result;
                    });
        });
    }

    private Collection<ProducerRecord<String, GenericRecord>> newProducerNotificationRequestRecordsWithCorrelationIdHeader(String topicName, ScheduledAssociationMeeting scheduledMeeting) {
        byte[] correlationId = scheduledMeeting.correlationId();
        Collection<ProducerRecord<String, GenericRecord>> records = new ArrayList<>();
        scheduledMeeting.participants().forEach(participant -> {
            boolean isSmsNotification = nonNull(participant.phoneNumber()) && StringUtils.hasText(participant.phoneNumber().value());
            boolean isEmailNotification = nonNull(participant.email()) && StringUtils.hasText(participant.email().value());
            if (isSmsNotification) {
                NotificationRequest notificationRequest = newNotificationRequest(scheduledMeeting, participant, "SMS");
                ProducerRecord<String, GenericRecord> record = newProducerRecordWithCorrelationIdHeader(topicName, notificationRequest, correlationId);
                records.add(record);
            }
            if (isEmailNotification) {
                NotificationRequest notificationRequest = newNotificationRequest(scheduledMeeting, participant, "EMAIL");
                ProducerRecord<String, GenericRecord> record = newProducerRecordWithCorrelationIdHeader(topicName, notificationRequest, correlationId);
                records.add(record);
            }
        });
        return records;
    }

    private NotificationRequest newNotificationRequest(ScheduledAssociationMeeting scheduledMeeting, Participant participant, String channel) {
        String date = scheduledMeeting.date().value();
        String time = scheduledMeeting.time().value();
        Long communityId = scheduledMeeting.communityId().value();

        return new NotificationRequest(
                MEETING_NOTIFICATION,
                new Recipient(
                        participant.id().value(),
                        channel,
                        org.propertymanagement.domain.notification.NotificationRequest.NotificationChannel.getChannelFrom(channel) == SMS
                                ? participant.phoneNumber().value() : participant.email().value(),
                        participant.name().value()
                ),
                date,
                time,
                communityId
        );
    }

    private ProducerRecord<String, GenericRecord> newProducerRecordWithCorrelationIdHeader(String topicName, SpecificRecordBase value, byte[] correlationId) {
        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(topicName, value);
        record.headers().add(KafkaHeaders.CORRELATION_ID, correlationId);
        return record;
    }

    private RetryDetails retrySendToKafka(ProducerRecord<String, GenericRecord> record) {
        AtomicBoolean isNotified = new AtomicBoolean(false);
        final AtomicInteger attempt = new AtomicInteger(0);
        final AtomicBoolean isSent = new AtomicBoolean(false);
        String reason = "Unknown";
        Throwable cause = null;
        do {
            attempt.incrementAndGet();
            try {
                log.debug("About to re-try send message {} of {}", attempt.get(), MAX_RETRY_ATTEMPTS);
                kafkaTemplate.send(record)
                        .get(timeout, TimeUnit.SECONDS);  // Block until finishing
                isSent.set(true);
            } catch (Exception ex) {
                if (ExceptionUtils.getRootCause(ex) instanceof TimeoutException timeoutException) {
                    cause = timeoutException;
                    reason = ex.getMessage() + " ||" + ex.getCause().getMessage();
                    log.error("Timeout retry {} of {} Topic={} CorrelationId={} Reason={}", attempt.get(), MAX_RETRY_ATTEMPTS, record.topic(), correlationIdAsString(record), reason);
                }
            }
        } while (attempt.get() < MAX_RETRY_ATTEMPTS && !isSent.get());
        isNotified.set(isSent.get());

        boolean successful = isNotified.get();
        return new RetryDetails(successful, attempt.get(), correlationIdAsString(record), record.topic(), !successful ? reason : null, cause);
    }

    record RetryDetails(boolean successful, int attempts, String correlationId, String topic, String reason,
                        Throwable cause) {
    }

    private org.propertymanagement.associationmeeting.v1.MeetingInvite toAvroMeetingInvite(MeetingInvite invite) {
        var avroMeetingInvite = new org.propertymanagement.associationmeeting.v1.MeetingInvite();
        Optional.ofNullable(invite.getApproverId()).ifPresent(approverId -> avroMeetingInvite.setApproverId(String.valueOf(approverId.value())));
        avroMeetingInvite.setCommunityId(invite.getCommunityId().value());
        avroMeetingInvite.setTrackerId(invite.getTrackerId().toString());
        avroMeetingInvite.setDate(invite.getDate().value());
        avroMeetingInvite.setTime(invite.getTime().value());
        avroMeetingInvite.setApprovalDateTime(invite.getApprovalDateTime());
        return avroMeetingInvite;
    }
}
