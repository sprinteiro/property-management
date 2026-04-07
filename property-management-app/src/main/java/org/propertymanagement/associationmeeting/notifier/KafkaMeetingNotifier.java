package org.propertymanagement.associationmeeting.notifier;

import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TimeoutException;
import org.propertymanagement.associationmeeting.notification.MeetingNotification;
import org.propertymanagement.domain.MeetingInvite;
import org.propertymanagement.domain.Participant;
import org.propertymanagement.domain.ScheduledAssociationMeeting;
import org.propertymanagement.notification.v1.NotificationRequest;
import org.propertymanagement.notification.v1.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

public class KafkaMeetingNotifier implements MeetingNotification {
    private static final Logger log = LoggerFactory.getLogger(KafkaMeetingNotifier.class);
    public static final String TOPIC_NOTIFICATION_REQUEST = "notification-request";
    public static final String MEETING_NOTIFICATION = "MEETING_NOTIFICATION";
    private static final int MAX_RETRY_ATTEMPTS = 2;
    @Value("${kafka.retry.timeout:120000}")
    private int timeout;
    private final KafkaTemplate<String, GenericRecord> kafkaTemplate;
    private final Executor kafkaExecutor;

    public KafkaMeetingNotifier(KafkaTemplate<String, GenericRecord> kafkaTemplate, Executor kafkaExecutor) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaExecutor = kafkaExecutor;
    }

    @Override
    public void notifyForCreation(MeetingInvite invite) {
        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(TOPIC_MEETING_REGISTRATION_REQUEST, toAvroMeetingInvite(invite));
        CompletableFuture.runAsync(() -> {
                    log.info("Publishing invite for creation. Topic={} Message={}", record.topic(), record.value());
                    kafkaTemplate.send(record).handle((result, throwable) -> {
                        if (ExceptionUtils.getRootCause(throwable) instanceof TimeoutException) {
                            RetryDetails retryResult = retrySendToKafka(record);
                            log.info("Retry result - {}", retryResult);
                        } else if (nonNull(result)) {
                            log.info("Produced event to topic {} Message={}", record.topic(), record.value());
                        }
                        return result;
                    });
                }, kafkaExecutor
        );
    }

    @Override
    public void notifyForApproval(MeetingInvite invite) {
        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(TOPIC_MEETING_APPROVAL_REQUEST, toAvroMeetingInvite(invite));
        CompletableFuture.runAsync(() -> {
                    log.info("Publishing invite for approval. Topic={} Message={}", record.topic(), record.value());
                    kafkaTemplate.send(record).handle((result, throwable) -> {
                        if (ExceptionUtils.getRootCause(throwable) instanceof TimeoutException) {
                            RetryDetails retryResult = retrySendToKafka(record);
                            log.info("Retry result - {}", retryResult);
                        } else if (nonNull(result)) {
                            log.info("Produced event to topic {} Message={}", record.topic(), record.value());
                        }
                        return result;
                    });
                }, kafkaExecutor
        );
    }

    @Override
    public void notifyMeetingToParticipants(ScheduledAssociationMeeting scheduledMeeting) {
        Collection<ProducerRecord<String, GenericRecord>> records =
                newProducerNotificationRequestRecords(TOPIC_NOTIFICATION_REQUEST, scheduledMeeting);

        List<CompletableFuture<Void>> futures = records.stream()
                .map(record -> CompletableFuture.runAsync(() -> {
                    log.info("Publishing notification request. Topic={} Message={}",
                            record.topic(), record.value());
                    try {
                        kafkaTemplate.send(record).get(timeout, TimeUnit.MILLISECONDS);
                        log.info("Produced event to topic {} Message={}", record.topic(), record.value());
                    } catch (Exception e) {
                        log.error("Error sending notification to Kafka.", e);
                        throw new RuntimeException(e);
                    }
                }, kafkaExecutor))
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Failure in notification fan-out: {}", e.getMessage());
            throw new RuntimeException("Failed to notify all participants", e);
        }
    }

    private Collection<ProducerRecord<String, GenericRecord>> newProducerNotificationRequestRecords(String topicName, ScheduledAssociationMeeting scheduledMeeting) {
        Collection<ProducerRecord<String, GenericRecord>> records = new ArrayList<>();
        scheduledMeeting.participants().forEach(participant -> {
            boolean isSmsNotification = nonNull(participant.phoneNumber()) && StringUtils.hasText(participant.phoneNumber().value());
            boolean isEmailNotification = nonNull(participant.email()) && StringUtils.hasText(participant.email().value());
            if (isSmsNotification) {
                NotificationRequest notificationRequest = newNotificationRequest(scheduledMeeting, participant, "SMS");
                records.add(new ProducerRecord<>(topicName, notificationRequest));
            }
            if (isEmailNotification) {
                NotificationRequest notificationRequest = newNotificationRequest(scheduledMeeting, participant, "EMAIL");
                records.add(new ProducerRecord<>(topicName, notificationRequest));
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

    private RetryDetails retrySendToKafka(ProducerRecord<String, GenericRecord> record) {
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
                    log.error("Timeout retry {} of {} Topic={} Reason={}", attempt.get(), MAX_RETRY_ATTEMPTS, record.topic(), reason);
                }
            }
        } while (attempt.get() < MAX_RETRY_ATTEMPTS && !isSent.get());

        boolean successful = isSent.get();
        return new RetryDetails(successful, attempt.get(), record.topic(), !successful ? reason : null, cause);
    }

    record RetryDetails(boolean successful, int attempts, String topic, String reason,
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
