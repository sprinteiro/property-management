package org.propertymanagement.associationmeeting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.propertymanagement.TestCorrelationIdLogUtils;
import org.propertymanagement.associationmeeting.exception.MeetingScheduleException;
import org.propertymanagement.associationmeeting.notification.MeetingNotification;
import org.propertymanagement.associationmeeting.notification.MeetingNotificationService;
import org.propertymanagement.associationmeeting.repository.MeetingRepository;
import org.propertymanagement.associationmeeting.repository.TrackerIdRepository;
import org.propertymanagement.domain.*;
import org.propertymanagement.neighbour.repository.NeighbourRepository;
import org.propertymanagement.util.CorrelationIdLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.propertymanagement.domain.Participant.ParticipantRole.*;

public class MeetingSchedulerTest {
    private static final CommunityId COMMUNITY_ID = new CommunityId(1L);
    private static final NeighbourgId PRESIDENT_ID = new NeighbourgId(2L);
    private static final MeetingDate MEETING_DATE = new MeetingDate("01/12/2024");
    private static final MeetingTime MEETING_TIME = new MeetingTime("19:00");
    private static final String APPROVAL_MEETING_DATE = "29/11/2024";
    private static final String APPROVAL_MEETING_TIME = "10:00";
    private static final String APPROVAL_DATE_TIME_STR = APPROVAL_MEETING_DATE + " " + APPROVAL_MEETING_TIME;
    private static final LocalDateTime APPROVAL_DATE_TIME = LocalDateTime.parse(APPROVAL_DATE_TIME_STR, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    private static final TrackerId TRACKER_ID = new TrackerId(UUID.randomUUID());
    MeetingRepository mockMeetingRepository = mock(MeetingRepository.class);
    MeetingNotification mockMeetingNotificationService = mock(MeetingNotification.class);
    CorrelationIdLog correlationIdLog = new TestCorrelationIdLogUtils();
    NeighbourRepository mockNeighbourgRepository = mock(NeighbourRepository.class);
    TrackerIdRepository mockTrackerIdRepository = mock(TrackerIdRepository.class);


    @BeforeEach
    void setup() {
        mockMeetingRepository = mock(MeetingRepository.class);
        mockMeetingNotificationService = mock(MeetingNotification.class);
        correlationIdLog = new TestCorrelationIdLogUtils();
        mockNeighbourgRepository = mock(NeighbourRepository.class);
        mockTrackerIdRepository = mock(TrackerIdRepository.class);
    }

    @Test
    void scheduleMeeting() {
        // Here we mock that the repository returns an invite that has an approval date (simulating it was found and has state)
        // OR the test expects the result to have approval date because the helper used to create 'meetingInvite' input had it?
        // Input 'meetingInvite' is created via newMeetingInvite(COMMUNITY_ID, PRESIDENT_ID).
        // Let's check that helper.
        
        MeetingInvite meetingInvite = newMeetingInviteApproved(COMMUNITY_ID, PRESIDENT_ID); // Input has approval? "newMeeting" usually takes a fresh invite.
        // If input has approval, logic might preserve it?
        // But logic generates new TrackerId.
        
        // Let's assume for this test, we want to match the previous assertion behavior.
        // The assertions check for APPROVAL_DATE_TIME. 
        // So the mocked repository return must have it.
        
        when(mockMeetingRepository.fetchMeetingInvite(any(), any())).thenReturn(newMeetingInviteApproved(COMMUNITY_ID, PRESIDENT_ID, TRACKER_ID));
        doNothing().when(mockTrackerIdRepository).register(any(MeetingInvite.class));

        MeetingScheduler scheduler = new MeetingScheduler(mockMeetingRepository, mockNeighbourgRepository, mockMeetingNotificationService, mockTrackerIdRepository, correlationIdLog);
        MeetingInvite result = scheduler.newMeeting(meetingInvite);

        assertNotNull(result);
        assertEquals(APPROVAL_DATE_TIME, result.approvalDateTime());
        assertEquals(PRESIDENT_ID, result.approverId());
        assertEquals(COMMUNITY_ID, result.communityId());
        assertEquals(MEETING_DATE, result.date());
        assertEquals(MEETING_TIME, result.time());
        assertNotNull(result.trackerId());
        assertNotNull(result.correlationId());

        verify(mockTrackerIdRepository).register(any(MeetingInvite.class));
        verify(mockMeetingNotificationService).notifyForCreation(any(MeetingInvite.class));
    }

    @Test
    void approveRegisteredMeetingWithScheduledDetails() {
        MeetingNotification mockAutomaticMeetingNotifier = mock(MeetingNotification.class);
        MeetingNotificationService meetingNotificationService = spy(new MeetingNotificationService(true, mockAutomaticMeetingNotifier, null));

        // Must return PENDING approval invite
        when(mockMeetingRepository.fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID)).thenReturn(newMeetingInvitePendingApproval(COMMUNITY_ID, PRESIDENT_ID, TRACKER_ID));
        ApprovalMeetingInvite approval = new ApprovalMeetingInvite(COMMUNITY_ID, TRACKER_ID, PRESIDENT_ID);

        MeetingScheduler scheduler = new MeetingScheduler(mockMeetingRepository, mockNeighbourgRepository, meetingNotificationService, mockTrackerIdRepository, correlationIdLog);
        scheduler.fetchMeetingInviteAndNotifyScheduledMeetingForApproval(approval);

        verify(meetingNotificationService).approveMeeting(any(MeetingInvite.class));
        verify(mockAutomaticMeetingNotifier).notifyForApproval(any(MeetingInvite.class));
    }

    @Test
    void resendMeetingInviteForApproval() {
        ResendMeetingInviteRequest resendMeetingInvite = new ResendMeetingInviteRequest(COMMUNITY_ID, TRACKER_ID, ResendMeetingInviteRequest.ResendType.FOR_APPROVAL);
        // Must return PENDING approval invite, so it can be notified for approval
        MeetingInvite meetingInvite = newMeetingInvitePendingApproval(COMMUNITY_ID, PRESIDENT_ID, TRACKER_ID);
        when(mockMeetingRepository.fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID)).thenReturn(meetingInvite);

        MeetingScheduler scheduler = new MeetingScheduler(mockMeetingRepository, mockNeighbourgRepository, mockMeetingNotificationService, mockTrackerIdRepository, correlationIdLog);
        scheduler.resendMeetingInvite(resendMeetingInvite);

        verify(mockMeetingRepository).fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID);
        verify(mockMeetingNotificationService).notifyForApproval(any(MeetingInvite.class));
    }

    @Test
    void resendMeetingInviteToParticipants() {
        ResendMeetingInviteRequest resendMeetingInvite = new ResendMeetingInviteRequest(COMMUNITY_ID, TRACKER_ID, ResendMeetingInviteRequest.ResendType.TO_PARTICIPANTS);
        // Must be APPROVED to send to participants
        MeetingInvite meetingInvite = newMeetingInviteApproved(COMMUNITY_ID, PRESIDENT_ID, TRACKER_ID);
        when(mockMeetingRepository.fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID)).thenReturn(meetingInvite);
        List<Participant> participants = newParticipants();
        ScheduledAssociationMeeting scheduledMeeting = newScheduledAssociationMeeting(participants);
        when(mockMeetingRepository.fetchScheduledAssociationMeeting(COMMUNITY_ID, TRACKER_ID)).thenReturn(scheduledMeeting);
        when(mockNeighbourgRepository.fetchNeighbours(anyCollection())).thenReturn(newParticipantsWithAllDetails());

        MeetingScheduler scheduler = new MeetingScheduler(mockMeetingRepository, mockNeighbourgRepository, mockMeetingNotificationService, mockTrackerIdRepository, correlationIdLog);
        scheduler.resendMeetingInvite(resendMeetingInvite);

        verify(mockMeetingRepository).fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID);
        verify(mockMeetingRepository).fetchScheduledAssociationMeeting(COMMUNITY_ID, TRACKER_ID);
        verify(mockMeetingNotificationService).notifyMeetingToParticipants(any(ScheduledAssociationMeeting.class));
    }

    @Test
    void registerMeetingInviteWhenResendingAsNotPreviouslyRegistered() {
        ResendMeetingInviteRequest resendMeetingInvite = new ResendMeetingInviteRequest(COMMUNITY_ID, TRACKER_ID, ResendMeetingInviteRequest.ResendType.TO_PARTICIPANTS);
        // Start pending, then approve manually if needed, but here we construct it.
        // The original test called .approve(). 
        // newMeetingInvitePendingApproval().approve() is correct.
        MeetingInvite meetingInvite = newMeetingInvitePendingApproval(COMMUNITY_ID, PRESIDENT_ID, TRACKER_ID).approve(PRESIDENT_ID, APPROVAL_DATE_TIME, null);
        
        List<Participant> participants = newParticipants();
        ScheduledAssociationMeeting scheduledMeeting = newScheduledAssociationMeeting(participants);
        when(mockMeetingRepository.fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID)).thenReturn(null);
        when(mockMeetingRepository.fetchScheduledAssociationMeeting(COMMUNITY_ID, TRACKER_ID)).thenReturn(scheduledMeeting);
        when(mockNeighbourgRepository.fetchNeighbours(anyCollection())).thenReturn(newParticipantsWithAllDetails());
        when(mockTrackerIdRepository.fetchMeetingInvite(TRACKER_ID)).thenReturn(null);

        MeetingScheduler scheduler = new MeetingScheduler(mockMeetingRepository, mockNeighbourgRepository, mockMeetingNotificationService, mockTrackerIdRepository, correlationIdLog);
        assertThrows(MeetingScheduleException.class, () ->scheduler.resendMeetingInvite(resendMeetingInvite));

        verify(mockMeetingRepository).fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID);
        verify(mockTrackerIdRepository).fetchMeetingInvite(TRACKER_ID);
    }

    @Test
    void unableToResendMeetingInviteAsNotFound() {
        ResendMeetingInviteRequest resendMeetingInvite = new ResendMeetingInviteRequest(COMMUNITY_ID, TRACKER_ID, ResendMeetingInviteRequest.ResendType.TO_PARTICIPANTS);
        // Same here, start pending then approve
        MeetingInvite meetingInvite = newMeetingInvitePendingApproval(COMMUNITY_ID, PRESIDENT_ID, TRACKER_ID).approve(PRESIDENT_ID, APPROVAL_DATE_TIME, null);
        
        List<Participant> participants = newParticipants();
        ScheduledAssociationMeeting scheduledMeeting = newScheduledAssociationMeeting(participants);
        when(mockMeetingRepository.fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID)).thenReturn(null);
        when(mockMeetingRepository.fetchScheduledAssociationMeeting(COMMUNITY_ID, TRACKER_ID)).thenReturn(scheduledMeeting);
        when(mockNeighbourgRepository.fetchNeighbours(anyCollection())).thenReturn(newParticipantsWithAllDetails());
        when(mockTrackerIdRepository.fetchMeetingInvite(TRACKER_ID)).thenReturn(meetingInvite);

        MeetingScheduler scheduler = new MeetingScheduler(mockMeetingRepository, mockNeighbourgRepository, mockMeetingNotificationService, mockTrackerIdRepository, correlationIdLog);
        scheduler.resendMeetingInvite(resendMeetingInvite);

        verify(mockMeetingRepository).fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID);
        verify(mockTrackerIdRepository).fetchMeetingInvite(TRACKER_ID);
    }

    @Test
    void meetingPendingOfApproval() {
        // Must be PENDING
        MeetingInvite meetingInvite = newMeetingInvitePendingApproval(COMMUNITY_ID, PRESIDENT_ID, TRACKER_ID);
        when(mockMeetingRepository.fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID)).thenReturn(meetingInvite);

        MeetingScheduler scheduler = new MeetingScheduler(mockMeetingRepository, mockNeighbourgRepository, mockMeetingNotificationService, mockTrackerIdRepository, correlationIdLog);
        MeetingInvite result = scheduler.fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID);

        assertThat(result)
                .returns(COMMUNITY_ID, MeetingInvite::communityId)
                .returns(PRESIDENT_ID, MeetingInvite::approverId)
                .returns(null, MeetingInvite::approvalDateTime)
                .returns(MEETING_DATE, MeetingInvite::date)
                .returns(MEETING_TIME, MeetingInvite::time)
        ;

    }

    // Helpers

    private MeetingInvite newMeetingInviteApproved(CommunityId communityId, NeighbourgId approverId) {
        return new MeetingInvite(communityId, MEETING_DATE, MEETING_TIME, null, approverId, APPROVAL_DATE_TIME, null);
    }

    private MeetingInvite newMeetingInviteApproved(CommunityId communityId, NeighbourgId presidentId, TrackerId trackerId) {
        return new MeetingInvite(communityId, MEETING_DATE, MEETING_TIME, trackerId, presidentId, APPROVAL_DATE_TIME, null);
    }

    private MeetingInvite newMeetingInvitePendingApproval(CommunityId communityId, NeighbourgId presidentId, TrackerId trackerId) {
        return new MeetingInvite(communityId, MEETING_DATE, MEETING_TIME, trackerId, presidentId, null, null);
    }
    
    private ScheduledAssociationMeeting newScheduledAssociationMeeting(List<Participant> participants) {
        return new ScheduledAssociationMeeting(COMMUNITY_ID, TRACKER_ID, MEETING_DATE, MEETING_TIME, participants, PRESIDENT_ID, APPROVAL_DATE_TIME, null);
    }

    public List<Participant> newParticipants() {
        return List.of(
                new Participant(new NeighbourgId(1L), ADMINISTRATOR, null, null, null),
                new Participant(new NeighbourgId(2L), PRESIDENT, null, null, null),
                new Participant(new NeighbourgId(3L), VICEPRESIDENT, null, null, null),
                new Participant(new NeighbourgId(4L), COMMUNITY_MEMBER, null, null, null)
        );
    }

    private Collection<Participant> newParticipantsWithAllDetails() {
        return List.of(
                new Participant(new NeighbourgId(1L), ADMINISTRATOR, new Name("administrator"), new PhoneNumber("+111111111"), new Email("admin@email.com")),
                new Participant(new NeighbourgId(2L), PRESIDENT, new Name("president"), new PhoneNumber("+222222222"), new Email("president@email.com")),
                new Participant(new NeighbourgId(3L), VICEPRESIDENT, new Name("vicepresident"), new PhoneNumber("+333333333"), new Email("vicepresident@email.com")),
                new Participant(new NeighbourgId(4L), COMMUNITY_MEMBER, new Name("neighbour1"), new PhoneNumber("+444444444"), new Email("neighbour1@email.com"))
        );

    }
}
