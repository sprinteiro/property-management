package org.propertymanagement.associationmeeting.notification;

import org.propertymanagement.domain.MeetingInvite;
import org.propertymanagement.domain.ScheduledAssociationMeeting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MeetingNotificationService implements MeetingNotification {
    private static final Logger log = LoggerFactory.getLogger(MeetingNotificationService.class);
    private final boolean automaticApproval;
    private final MeetingNotification kafkaMeetingNotifier;
    private final MeetingNotification manualMeetingNotifier;


    public MeetingNotificationService(
            boolean automaticApproval,
            MeetingNotification kafkaMeetingNotifier,
            MeetingNotification manualMeetingNotifier) {
        this.automaticApproval = automaticApproval;
        this.kafkaMeetingNotifier = kafkaMeetingNotifier;
        this.manualMeetingNotifier = manualMeetingNotifier;
    }

    @Override
    public void notifyForApproval(MeetingInvite invite) {
        if (automaticApproval) {
            kafkaMeetingNotifier.notifyForApproval(invite);
        } else {
            log.info("Skipping automatic approval as disabled.");
            manualMeetingNotifier.notifyForApproval(invite);
        }
    }

    @Override
    public void notifyMeetingToParticipants(ScheduledAssociationMeeting scheduledMeeting) {
        kafkaMeetingNotifier.notifyMeetingToParticipants(scheduledMeeting);
    }

    @Override
    public void notifyForCreation(MeetingInvite invite) {
        kafkaMeetingNotifier.notifyForCreation(invite);
    }

    public void approveMeeting(MeetingInvite invite) {
        kafkaMeetingNotifier.notifyForApproval(invite);
    }

}
