package org.propertymanagement.associationmeeting.notifier;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

public class KafkaMeetingNotifier implements MeetingNotification {
    private static final Logger log = LoggerFactory.getLogger(KafkaMeetingNotifier.class);
    public static final String TOPIC_NOTIFICATION_REQUEST = "notification-request";
    public static final String MEETING_NOTIFICATION = "MEETING_NOTIFICATION";
    private static final int MAX_RETRY_ATTEMPTS = 2;
    @Value("${kafka.retry.timeout:120000}")
    private int timeout;
    private final KafkaTemplate<String, GenericRecord> kafkaTemplate;
    private final Executor kafkaExecutor;
    private final CorrelationIdLog correlationIdLog;

    public KafkaMeetingNotifier(KafkaTemplate<String, GenericRecord> kafkaTemplate, Executor kafkaExecutor, CorrelationIdLog correlationIdLog) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaExecutor = kafkaExecutor;
        this.correlationIdLog = correlationIdLog;
    }

    @Override
    public void notifyForCreation(MeetingInvite invite) {
        correlationIdLog.execWithProvidedCorrelationId(correlationIdAsString(invite.correlationId()), () -> {
                    Map<String, String> copyOfContextMap = correlationIdLog.getCopyOfContextMap();
                    ProducerRecord<String, GenericRecord> record = newProducerRecordWithCorrelationIdHeader(TOPIC_MEETING_REGISTRATION_REQUEST, toAvroMeetingInvite(invite), invite.correlationId());
                    CompletableFuture.supplyAsync(() -> {
                                correlationIdLog.setContextMap(copyOfContextMap);
                                log.info("Publishing invite for creation. CorrelationId={} Topic={} Message={}", correlationIdAsString(invite.correlationId()), record.topic(), record.value());
                                return kafkaTemplate.send(record);
                            }, kafkaExecutor)
                            .handle((result, throwable) -> {
                                if (ExceptionUtils.getRootCause(throwable) instanceof TimeoutException) {
                                    RetryDetails retryResult = retrySendToKafka(record);
                                    log.info("Retry result - {} CorrelationId={}", retryResult, correlationIdAsString(invite.correlationId()));
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
        correlationIdLog.execWithProvidedCorrelationId(correlationIdAsString(invite.correlationId()), () -> {
                    Map<String, String> copyOfContextMap = correlationIdLog.getCopyOfContextMap();
                    ProducerRecord<String, GenericRecord> record = newProducerRecordWithCorrelationIdHeader(TOPIC_MEETING_APPROVAL_REQUEST, toAvroMeetingInvite(invite), invite.correlationId());
                    CompletableFuture.supplyAsync(() -> {
                                correlationIdLog.setContextMap(copyOfContextMap);
                                log.info("Publishing invite for approval. CorrelationId={} Topic={} Message={}", correlationIdAsString(invite.correlationId()), record.topic(), record.value());
                                return kafkaTemplate.send(record);
                            }, kafkaExecutor)
                            .handle((result, throwable) -> {
                                if (ExceptionUtils.getRootCause(throwable) instanceof TimeoutException) {
                                    RetryDetails retryResult = retrySendToKafka(record);
                                    log.info("Retry result - {} CorrelationId={}", retryResult, correlationIdAsString(invite.correlationId()));
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
        var correlationId = correlationIdAsString(scheduledMeeting.correlationId());
        correlationIdLog.execWithProvidedCorrelationId(correlationId, () -> {
            Map<String, String> copyOfContextMap = correlationIdLog.getCopyOfContextMap();
            Collection<ProducerRecord<String, GenericRecord>> records =
                    newProducerNotificationRequestRecordsWithCorrelationIdHeader(TOPIC_NOTIFICATION_REQUEST, scheduledMeeting);

            List<CompletableFuture<Void>> futures = records.stream()
                    .map(record -> CompletableFuture.runAsync(() -> {
                        correlationIdLog.setContextMap(copyOfContextMap);
                        log.info("Publishing notification request. CorrelationId={} Topic={} Message={}",
                                correlationId, record.topic(), record.value());
                        try {
                            kafkaTemplate.send(record).get(timeout, TimeUnit.MILLISECONDS);
                            log.info("Produced event to topic {} CorrelationId={} Message={}", record.topic(), correlationId, record.value());
                        } catch (Exception e) {
                            log.error("Error sending notification to Kafka. CorrelationId={}", correlationId, e);
                            throw new RuntimeException(e);
                        }
                    }, kafkaExecutor))
                    .toList();

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .get(timeout, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("Failure in notification fan-out for CorrelationId {}: {}", correlationId, e.getMessage());
                throw new RuntimeException("Failed to notify all participants", e);
            }
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
        var recipient = new Recipient();
        recipient.setId(participant.id().value());
        recipient.setChannel(channel);
        recipient.setAddress(org.propertymanagement.domain.notification.NotificationRequest.NotificationChannel.getChannelFrom(channel) == SMS
                ? participant.phoneNumber().value() : participant.email().value());
        recipient.setName(participant.name().value());

        var request = new NotificationRequest();
        request.setNotificationType(MEETING_NOTIFICATION);
        request.setRecipient(recipient);
        request.setDate(scheduledMeeting.date().value());
        request.setTime(scheduledMeeting.time().value());
        request.setCommunityId(scheduledMeeting.communityId().value());

        return request;
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
        Optional.ofNullable(invite.approverId()).ifPresent(approverId -> avroMeetingInvite.setApproverId(String.valueOf(approverId.value())));
        avroMeetingInvite.setCommunityId(invite.communityId().value());
        avroMeetingInvite.setTrackerId(invite.trackerId().toString());
        avroMeetingInvite.setDate(invite.date().value());
        avroMeetingInvite.setTime(invite.time().value());
        if (invite.approvalDateTime() != null) {
            avroMeetingInvite.setApprovalDateTime(invite.approvalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        return avroMeetingInvite;
    }
}
