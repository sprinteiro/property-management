package org.propertymanagement.associationmeeting.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.propertymanagement.associationmeeting.MeetingScheduler;
import org.propertymanagement.associationmeeting.repository.MeetingRepository;
import org.propertymanagement.associationmeeting.v1.MeetingInvite;
import org.propertymanagement.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static java.util.Objects.isNull;
import static org.propertymanagement.associationmeeting.config.KafkaTopicsConfig.TOPIC_MEETING_APPROVAL_REQUEST;

public class KafkaMeetingApprovalListener {
    private static final Logger log = LoggerFactory.getLogger(KafkaMeetingApprovalListener.class);

    private final MeetingRepository meetingRepository;
    private final MeetingScheduler meetingScheduler;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public KafkaMeetingApprovalListener(MeetingRepository meetingRepository, MeetingScheduler meetingScheduler) {
        this.meetingRepository = meetingRepository;
        this.meetingScheduler = meetingScheduler;
    }

    @KafkaListener(topics = {TOPIC_MEETING_APPROVAL_REQUEST}, groupId = "${kafka.topic.group-id.meeting}")
    public void receiveMeetingForApproval(ConsumerRecord<String, MeetingInvite> record) {
        MeetingInvite invite = record.value();
        log.info("Received record={} Key={}", invite, record.key());

        var meetingInvite = toDomain(invite);
        ScheduledAssociationMeeting persistedScheduledMeeting = meetingRepository.fetchScheduledAssociationMeeting(meetingInvite.communityId(), meetingInvite.trackerId());
        if (isNull(persistedScheduledMeeting)) {
            log.warn("Skipping meeting for approval as not found. TrackerId={} CommunityId={} ApproverId={}", meetingInvite.trackerId().toString(), meetingInvite.communityId().value(), meetingInvite.approverId().value());
            return;
        }

        ScheduledAssociationMeeting approvedMeeting = persistedScheduledMeeting.approve(meetingInvite.approverId(), meetingInvite.approvalDateTime());
        meetingRepository.approveScheduledMeeting(approvedMeeting);

        meetingScheduler.notifyParticipants(approvedMeeting);
    }

    private org.propertymanagement.domain.MeetingInvite toDomain(MeetingInvite invite) {
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
                approvalDateTime
        );
    }
}
