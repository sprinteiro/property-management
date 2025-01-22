package org.propertymanagement.associationmeeting.notification;

import org.junit.jupiter.api.Test;
import org.propertymanagement.domain.CommunityId;
import org.propertymanagement.domain.MeetingInvite;
import org.propertymanagement.domain.ScheduledAssociationMeeting;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MeetingNotificationServiceTest {
    private static final CommunityId COMMUNITY_ID = new CommunityId(1L);

    @Test
    void notifyForApprovalAutomatically() {
        MeetingNotification automaticNotifier = mock(MeetingNotification.class);
        MeetingInvite meetingInvite = new MeetingInvite(COMMUNITY_ID);

        MeetingNotification meetingNotification = new MeetingNotificationService(true, automaticNotifier, null);
        meetingNotification.notifyForApproval(meetingInvite);

        verify(automaticNotifier).notifyForApproval(meetingInvite);
    }

    @Test
    void notifyForApprovalManually() {
        MeetingNotification manualNotifier = mock(MeetingNotification.class);
        MeetingInvite meetingInvite = new MeetingInvite(COMMUNITY_ID);

        MeetingNotification meetingNotification = new MeetingNotificationService(false, null, manualNotifier);
        meetingNotification.notifyForApproval(meetingInvite);

        verify(manualNotifier).notifyForApproval(meetingInvite);
    }

    @Test
    void notifyMeetingForCreation() {
        MeetingNotification automaticNotifier = mock(MeetingNotification.class);
        MeetingInvite meetingInvite = new MeetingInvite(COMMUNITY_ID);

        MeetingNotification meetingNotification = new MeetingNotificationService(true, automaticNotifier, null);
        meetingNotification.notifyForCreation(meetingInvite);

        verify(automaticNotifier).notifyForCreation(meetingInvite);
    }

    @Test
    void notifyMeetingToParticipants() {
        MeetingNotification automaticNotifier = mock(MeetingNotification.class);
        MeetingNotification meetingNotification = new MeetingNotificationService(true, automaticNotifier, null);

        ScheduledAssociationMeeting scheduledMeeting = new ScheduledAssociationMeeting(null, null, null, null);
        meetingNotification.notifyMeetingToParticipants(scheduledMeeting);

        verify(automaticNotifier).notifyMeetingToParticipants(scheduledMeeting);
    }

    @Test
    void approveMeeting() {
        MeetingNotification automaticNotifier = mock(MeetingNotification.class);
        MeetingInvite meetingInvite = new MeetingInvite(COMMUNITY_ID);

        MeetingNotificationService meetingNotification = new MeetingNotificationService(true, automaticNotifier, null);
        meetingNotification.approveMeeting(meetingInvite);

        verify(automaticNotifier).notifyForApproval(meetingInvite);
    }
}
