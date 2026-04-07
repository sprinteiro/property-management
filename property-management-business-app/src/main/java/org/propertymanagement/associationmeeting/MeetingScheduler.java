package org.propertymanagement.associationmeeting;

import org.propertymanagement.associationmeeting.exception.InvalidMeetingInviteException;
import org.propertymanagement.associationmeeting.exception.MeetingScheduleException;
import org.propertymanagement.associationmeeting.notification.MeetingNotification;
import org.propertymanagement.associationmeeting.notification.MeetingNotificationService;
import org.propertymanagement.associationmeeting.repository.MeetingRepository;
import org.propertymanagement.associationmeeting.repository.TrackerIdRepository;
import org.propertymanagement.associationmeeting.trackerid.DefaultTrackerIdManager;
import org.propertymanagement.associationmeeting.trackerid.TrackerIdManager;
import org.propertymanagement.domain.*;
import org.propertymanagement.neighbour.repository.NeighbourRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class MeetingScheduler {
    private static final Logger log = LoggerFactory.getLogger(MeetingScheduler.class);
    private static final int MINIMUM_PARTICIPANTS = 4;
    private final MeetingRepository meetingRepository;
    private final NeighbourRepository neighbourRepository;
    private final MeetingNotification meetingNotificationService;
    private final TrackerIdManager trackerIdManager;

    public MeetingScheduler(
            MeetingRepository meetingRepository,
            NeighbourRepository neighbourRepository,
            MeetingNotification meetingNotificationService,
            TrackerIdRepository trackerIdRepository
    ) {
        this.meetingRepository = meetingRepository;
        this.neighbourRepository = neighbourRepository;
        this.meetingNotificationService = meetingNotificationService;
        this.trackerIdManager = new DefaultTrackerIdManager(trackerIdRepository);
    }

    public MeetingInvite newMeeting(MeetingInvite meetingInvite) {
        TrackerId trackerId = trackerIdManager.generateId();
        MeetingInvite trackedInvite = meetingInvite.withTracker(trackerId);

        boolean isTrackerIdRegistered = trackerIdManager.registerId(trackedInvite);
        if (!isTrackerIdRegistered) {
            String errorMessage = String.format("Unable to register a new meeting invite for CommunityId=%d TrackerId generation failed", trackedInvite.communityId().value());
            throw new MeetingScheduleException(errorMessage);
        }

        MeetingInvite persistedInvite = meetingRepository.fetchMeetingInvite(trackedInvite.communityId(), trackedInvite.trackerId());

        notifyScheduledMeetingForRegistration(persistedInvite);
        return persistedInvite;
    }

    public void fetchMeetingInviteAndNotifyScheduledMeetingForApproval(ApprovalMeetingInvite approval) {
        checkValidApprovalMeetingInvite(approval);
        MeetingInvite meetingInvite = meetingRepository.fetchMeetingInvite(approval.getCommunityId(), approval.getTrackerId());
        checkValidApprover(approval, meetingInvite);
        checkValidInviteForApproval(meetingInvite);
        ((MeetingNotificationService) meetingNotificationService).approveMeeting(meetingInvite);
    }

    public void notifyScheduledMeetingForApproval(MeetingInvite invite) {
        checkValidInviteForApproval(invite);
        meetingNotificationService.notifyForApproval(invite);
    }

    public MeetingInvite fetchMeetingInvite(CommunityId communityId, TrackerId trackerId) {
        return Optional.ofNullable(meetingRepository.fetchMeetingInvite(communityId, trackerId))
                .orElseThrow(() -> {
                    String message = String.format("Meeting not found. TrackerId=%s CommunityId=%d", trackerId.toString(), communityId.value());
                    return new MeetingScheduleException(message);
                });
    }

    public void resendMeetingInvite(ResendMeetingInviteRequest resendRequest) {
        log.info("About to resend meeting invite. Type={} TrackerId={} CommunityId={}",
                resendRequest.type(), resendRequest.trackerId(), resendRequest.communityId());
        TrackerId trackerId = resendRequest.trackerId();
        CommunityId communityId = resendRequest.communityId();
        MeetingInvite persistedMeetingInvite = meetingRepository.fetchMeetingInvite(communityId, trackerId);

        if (isNull(persistedMeetingInvite)) {
            // The meeting invite was never sent as part of the registration process.
            log.info("About to fetch tracking details TrackerId={}", resendRequest.trackerId().toString());
            MeetingInvite persistedTrackedMeetingInvite = trackerIdManager.fetchMeetingInvite(resendRequest.trackerId());
            if (isNull(persistedTrackedMeetingInvite)) {
                String message = "Unable to resend meeting invite for approval. Not found.";
                throw new MeetingScheduleException(message);
            }
            notifyScheduledMeetingForRegistration(persistedTrackedMeetingInvite);
            return;
        }

        if (resendRequest.type() == ResendMeetingInviteRequest.ResendType.FOR_APPROVAL) {
            notifyScheduledMeetingForApproval(persistedMeetingInvite);
            return;
        }
        if (resendRequest.type() == ResendMeetingInviteRequest.ResendType.TO_PARTICIPANTS) {
            if (isNull(persistedMeetingInvite.approvalDateTime())) {
                String message = "Unable to resend meeting invite to participants. Approval date/time not found.";
                throw new MeetingScheduleException(message);
            }
            ScheduledAssociationMeeting persistedScheduledMeeting = meetingRepository.fetchScheduledAssociationMeeting(communityId, trackerId);
            if (isNull(persistedScheduledMeeting)) {
                String message = "Unable to resend meeting invite to participants. Participants not found.";
                throw new MeetingScheduleException(message);
            }

            notifyParticipants(persistedScheduledMeeting);
        }
    }

    public void notifyParticipants(ScheduledAssociationMeeting scheduledMeeting) {
        checkValidScheduledMeeting(scheduledMeeting);
        meetingNotificationService.notifyMeetingToParticipants(scheduledMeeting);
    }

    private void notifyScheduledMeetingForRegistration(MeetingInvite meetingInvite) {
        checkValidInviteForRegistration(meetingInvite);
        meetingNotificationService.notifyForCreation(meetingInvite);
    }

    private void checkValidScheduledMeeting(ScheduledAssociationMeeting scheduledMeeting) {
        if (isNull(scheduledMeeting.communityId())
                || isNull(scheduledMeeting.date())
                || isNull(scheduledMeeting.time())
                || isNull(scheduledMeeting.participants())
                || scheduledMeeting.participants().size() < MINIMUM_PARTICIPANTS
        ) {
            String message = "Invalid scheduled association meeting";
            throw new InvalidMeetingInviteException(message);
        }
    }

    private void checkValidInviteForRegistration(MeetingInvite invite) {
        if (isNull(invite)
                || isNull(invite.communityId())
                || isNull(invite.date())
                || isNull(invite.time())
        ) {
            String message = "Invalid meeting invite for registration ";
            throw new InvalidMeetingInviteException(message);
        }
    }

    private void checkValidApprovalMeetingInvite(ApprovalMeetingInvite approval) {
        if (isNull(approval)
                || isNull(approval.getApproverId())
                || isNull(approval.getTrackerId())
        ) {
            throw new MeetingScheduleException("Invalid approval meeting invite.");
        }
    }

    private void checkValidInviteForApproval(MeetingInvite invite) {
        if (isNull(invite)
                || nonNull(invite.approvalDateTime())  // Assumption: the field contains a valued date and time content
                || isNull(invite.communityId())
                || isNull(invite.approverId())
                || isNull(invite.date())
                || isNull(invite.time())
                || isNull(invite.trackerId())
        ) {
            String message = "Invalid meeting invite for approval.";
            throw new InvalidMeetingInviteException(message);
        }
    }

    private void checkValidApprover(ApprovalMeetingInvite approval, MeetingInvite meetingInvite) {
        if (!approval.getApproverId().value().equals(meetingInvite.approverId().value())) {
            throw new IllegalArgumentException("Invalid approver");
        }
    }
}
