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

import java.util.UUID;

import static java.util.Objects.isNull;
import static org.propertymanagement.associationmeeting.config.KafkaTopicsConfig.TOPIC_MEETING_APPROVAL_REQUEST;

@Slf4j
@RequiredArgsConstructor
public class KafkaMeetingApprovalListener {
    private final MeetingRepository meetingRepository;
    private final MeetingScheduler meetingScheduler;
    private final CorrelationIdLog correlationIdLog;

    @KafkaListener(topics = {TOPIC_MEETING_APPROVAL_REQUEST}, groupId = "${kafka.topic.group-id.meeting}")
    public void receiveMeetingForApproval(ConsumerRecord<String, MeetingInvite> record, @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {
        correlationIdLog.execWithProvidedCorrelationId(CorrelationIdUtil.correlationIdAsString(correlationId), () -> {
            MeetingInvite invite = record.value();
            log.info("Received record={} Key={} CorrelationId={}", invite, record.key(), KafkaHeadersUtil.correlationIdAsString(correlationId));

            var meetingInvite = toDomain(invite, correlationId);
            ScheduledAssociationMeeting persistedScheduledMeeting = meetingRepository.fetchScheduledAssociationMeeting(meetingInvite.getCommunityId(), meetingInvite.getTrackerId());
            if (isNull(persistedScheduledMeeting)) {
                log.warn("Skipping meeting for approval as not found. TrackerId={} CommunityId={} ApproverId={}", meetingInvite.getTrackerId().toString(), meetingInvite.getCommunityId().value(), meetingInvite.getApproverId().value());
                return;
            }

            meetingRepository.approveScheduledMeeting(meetingInvite.getCommunityId(), meetingInvite.getTrackerId(), meetingInvite.getApproverId());
            ScheduledAssociationMeeting approvedMeetingWithCorrelationId = new ScheduledAssociationMeeting(persistedScheduledMeeting, correlationId);
            meetingScheduler.notifyParticipants(approvedMeetingWithCorrelationId);
        });
    }

    private org.propertymanagement.domain.MeetingInvite toDomain(MeetingInvite invite, byte[] correlationId) {
        var domainMeetingInvite = new org.propertymanagement.domain.MeetingInvite(
                new CommunityId(invite.getCommunityId()),
                new MeetingDate(invite.getDate()),
                new MeetingTime(invite.getTime()));
        domainMeetingInvite.setTrackerId(new TrackerId(UUID.fromString(invite.getTrackerId())));
        domainMeetingInvite.setApproverId(new NeighbourgId(Long.valueOf(invite.getApproverId())));
        domainMeetingInvite.setApprovalDateTime(invite.getApprovalDateTime());
        domainMeetingInvite.setCorrelationId(correlationId);
        return domainMeetingInvite;
    }
}
