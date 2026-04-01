package org.propertymanagement.associationmeeting.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.propertymanagement.associationmeeting.MeetingScheduler;
import org.propertymanagement.associationmeeting.repository.MeetingRepository;
import org.propertymanagement.associationmeeting.v1.MeetingInvite;
import org.propertymanagement.domain.*;
import org.propertymanagement.util.CorrelationIdLog;
import org.propertymanagement.util.CorrelationIdUtil;
import org.propertymanagement.util.KafkaHeadersUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.util.Optional;
import java.util.UUID;

import static org.propertymanagement.associationmeeting.config.KafkaTopicsConfig.TOPIC_MEETING_REGISTRATION_REQUEST;

public class KafkaMeetingCreationListener {
    private static final Logger log = LoggerFactory.getLogger(KafkaMeetingCreationListener.class);
    private final MeetingRepository meetingRepository;
    private final MeetingScheduler meetingScheduler;
    private final CorrelationIdLog correlationIdLog;

    public KafkaMeetingCreationListener(MeetingRepository meetingRepository, MeetingScheduler meetingScheduler, CorrelationIdLog correlationIdLog) {
        this.meetingRepository = meetingRepository;
        this.meetingScheduler = meetingScheduler;
        this.correlationIdLog = correlationIdLog;
    }

    @KafkaListener(topics = {TOPIC_MEETING_REGISTRATION_REQUEST}, groupId = "${kafka.topic.group-id.meeting}")
    public void receiveMeetingForCreation(ConsumerRecord<String, MeetingInvite> record, @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {
        correlationIdLog.execWithProvidedCorrelationId(CorrelationIdUtil.correlationIdAsString(correlationId), () -> {
            MeetingInvite avroInvite = record.value();

            log.info("Received record={} Key={} CorrelationId={}", avroInvite, record.key(), KafkaHeadersUtil.correlationIdAsString(correlationId));
            org.propertymanagement.domain.MeetingInvite domainInvite = toDomain(avroInvite, correlationId);

            meetingRepository.registerMeetingInvite(domainInvite);
            meetingScheduler.notifyScheduledMeetingForApproval(domainInvite);
        });
    }

    private org.propertymanagement.domain.MeetingInvite toDomain(MeetingInvite invite, byte[] correlationId) {
        var approverId = Optional.ofNullable(invite.getApproverId())
                .map(Long::valueOf)
                .map(NeighbourgId::new)
                .orElse(null);

        var domainInvite = org.propertymanagement.domain.MeetingInvite.create(
                new CommunityId(invite.getCommunityId()),
                new MeetingDate(invite.getDate()),
                new MeetingTime(invite.getTime()),
                approverId
        );

        if (invite.getTrackerId() != null) {
            domainInvite = domainInvite.withTracker(new TrackerId(UUID.fromString(invite.getTrackerId())), correlationId);
        } else {
            domainInvite = domainInvite.withCorrelationId(correlationId);
        }

        return domainInvite;
    }
}
