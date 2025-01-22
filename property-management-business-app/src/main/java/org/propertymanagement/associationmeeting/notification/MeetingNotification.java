package org.propertymanagement.associationmeeting.notification;

import org.propertymanagement.domain.MeetingInvite;
import org.propertymanagement.domain.ScheduledAssociationMeeting;

public interface MeetingNotification {
    void notifyForApproval(MeetingInvite invite);
    void notifyMeetingToParticipants(ScheduledAssociationMeeting scheduledMeeting);
    void notifyForCreation(MeetingInvite invite);
}