package org.propertymanagement.associationmeeting.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.propertymanagement.associationmeeting.MeetingScheduler;
import org.propertymanagement.associationmeeting.repository.MeetingRepository;
import org.propertymanagement.associationmeeting.v1.MeetingInvite;
import org.propertymanagement.domain.*;
import org.propertymanagement.util.CorrelationIdLog;
import org.propertymanagement.util.CorrelationIdUtil;
import org.propertymanagement.util.KafkaHeadersUtil;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static java.util.Objects.isNull;
import static org.propertymanagement.associationmeeting.config.KafkaTopicsConfig.TOPIC_MEETING_APPROVAL_REQUEST;

@Slf4j
@RequiredArgsConstructor
public class KafkaMeetingApprovalListener {
    private final MeetingRepository meetingRepository;
    private final MeetingScheduler meetingScheduler;
    private final CorrelationIdLog correlationIdLog;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @KafkaListener(topics = {TOPIC_MEETING_APPROVAL_REQUEST}, groupId = "${kafka.topic.group-id.meeting}")
    public void receiveMeetingForApproval(ConsumerRecord<String, MeetingInvite> record, @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {
        correlationIdLog.execWithProvidedCorrelationId(CorrelationIdUtil.correlationIdAsString(correlationId), () -> {
            MeetingInvite invite = record.value();
            log.info("Received record={} Key={} CorrelationId={}", invite, record.key(), KafkaHeadersUtil.correlationIdAsString(correlationId));

            var meetingInvite = toDomain(invite, correlationId);
            ScheduledAssociationMeeting persistedScheduledMeeting = meetingRepository.fetchScheduledAssociationMeeting(meetingInvite.communityId(), meetingInvite.trackerId());
            if (isNull(persistedScheduledMeeting)) {
                log.warn("Skipping meeting for approval as not found. TrackerId={} CommunityId={} ApproverId={}", meetingInvite.trackerId().toString(), meetingInvite.communityId().value(), meetingInvite.approverId().value());
                return;
            }

            // The approve method on the domain object could contain business logic, though here it's just for state transition
            ScheduledAssociationMeeting approvedMeeting = persistedScheduledMeeting.approve(meetingInvite.approverId(), meetingInvite.approvalDateTime());
            meetingRepository.approveScheduledMeeting(approvedMeeting);

            ScheduledAssociationMeeting approvedMeetingWithCorrelationId = approvedMeeting.withCorrelationId(correlationId);
            meetingScheduler.notifyParticipants(approvedMeetingWithCorrelationId);
        });
    }

    private org.propertymanagement.domain.MeetingInvite toDomain(MeetingInvite invite, byte[] correlationId) {
        LocalDateTime approvalDateTime = null;
        if (invite.getApprovalDateTime() != null) {
            approvalDateTime = LocalDateTime.parse(invite.getApprovalDateTime(), DATE_TIME_FORMATTER);
        }
        return new org.propertymanagement.domain.MeetingInvite(
                new CommunityId(invite.getCommunityId()),
                new MeetingDate(invite.getDate()),
                new MeetingTime(invite.getTime()),
                new TrackerId(UUID.fromString(invite.getTrackerId())),
                new NeighbourgId(Long.valueOf(invite.getApproverId())),
                approvalDateTime,
                correlationId
        );
    }
}
