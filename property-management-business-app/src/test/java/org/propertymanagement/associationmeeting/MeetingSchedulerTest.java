package org.propertymanagement.associationmeeting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.propertymanagement.associationmeeting.exception.MeetingScheduleException;
import org.propertymanagement.associationmeeting.notification.MeetingNotification;
import org.propertymanagement.associationmeeting.notification.MeetingNotificationService;
import org.propertymanagement.associationmeeting.repository.MeetingRepository;
import org.propertymanagement.associationmeeting.repository.TrackerIdRepository;
import org.propertymanagement.domain.*;
import org.propertymanagement.neighbour.repository.NeighbourRepository;
import org.propertymanagement.util.CorrelationIdLog;

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
    private static final String APPROVAL_DATE_TIME = APPROVAL_MEETING_DATE + " " + APPROVAL_MEETING_TIME;
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
        MeetingInvite meetingInvite = newMeetingInvite(COMMUNITY_ID, PRESIDENT_ID);

        MeetingScheduler scheduler = new MeetingScheduler(mockMeetingRepository, mockNeighbourgRepository, mockMeetingNotificationService, mockTrackerIdRepository, correlationIdLog);
        MeetingInvite result = scheduler.newMeeting(meetingInvite);

        assertNotNull(result);
        assertEquals(APPROVAL_DATE_TIME, result.getApprovalDateTime());
        assertEquals(PRESIDENT_ID, result.getApproverId());
        assertEquals(COMMUNITY_ID, result.getCommunityId());
        assertEquals(MEETING_DATE, result.getDate());
        assertEquals(MEETING_TIME, result.getTime());
        assertNotNull(result.getTrackerId());
        assertNotNull(result.getCorrelationId());


        verify(mockTrackerIdRepository).register(meetingInvite);
        verify(mockMeetingNotificationService).notifyForCreation(meetingInvite);
    }

    @Test
    void approveRegisteredMeetingWithScheduledDetails() {
        MeetingNotification mockAutomaticMeetingNotifier = mock(MeetingNotification.class);
        MeetingNotificationService meetingNotificationService = spy(new MeetingNotificationService(true, mockAutomaticMeetingNotifier, null));

        when(mockMeetingRepository.fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID)).thenReturn(newMeetingInvite(COMMUNITY_ID, PRESIDENT_ID, TRACKER_ID));
        ApprovalMeetingInvite approval = new ApprovalMeetingInvite(COMMUNITY_ID, TRACKER_ID, PRESIDENT_ID);

        MeetingScheduler scheduler = new MeetingScheduler(mockMeetingRepository, mockNeighbourgRepository, meetingNotificationService, mockTrackerIdRepository, correlationIdLog);
        scheduler.fetchMeetingInviteAndNotifyScheduledMeetingForApproval(approval);

        verify(meetingNotificationService).approveMeeting(any(MeetingInvite.class));
        verify(mockAutomaticMeetingNotifier).notifyForApproval(any(MeetingInvite.class));
    }

    @Test
    void resendMeetingInviteForApproval() {
        ResendMeetingInviteRequest resendMeetingInvite = new ResendMeetingInviteRequest(COMMUNITY_ID, TRACKER_ID, ResendMeetingInviteRequest.ResendType.FOR_APPROVAL);
        MeetingInvite meetingInvite = newMeetingInvite(COMMUNITY_ID, PRESIDENT_ID, TRACKER_ID);
        when(mockMeetingRepository.fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID)).thenReturn(meetingInvite);

        MeetingScheduler scheduler = new MeetingScheduler(mockMeetingRepository, mockNeighbourgRepository, mockMeetingNotificationService, mockTrackerIdRepository, correlationIdLog);
        scheduler.resendMeetingInvite(resendMeetingInvite);

        verify(mockMeetingRepository).fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID);
        verify(mockMeetingNotificationService).notifyForApproval(meetingInvite);
    }

    @Test
    void resendMeetingInviteToParticipants() {
        ResendMeetingInviteRequest resendMeetingInvite = new ResendMeetingInviteRequest(COMMUNITY_ID, TRACKER_ID, ResendMeetingInviteRequest.ResendType.TO_PARTICIPANTS);
        MeetingInvite meetingInvite = newMeetingInvite(COMMUNITY_ID, PRESIDENT_ID, TRACKER_ID);
        meetingInvite.setApprovalDateTime(APPROVAL_DATE_TIME);
        when(mockMeetingRepository.fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID)).thenReturn(meetingInvite);
        List<Participant> participants = newParticipants();
        ScheduledAssociationMeeting scheduledMeeting = new ScheduledAssociationMeeting(COMMUNITY_ID, MEETING_DATE, MEETING_TIME, participants);
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
        MeetingInvite meetingInvite = newMeetingInvite(COMMUNITY_ID, PRESIDENT_ID, TRACKER_ID);
        meetingInvite.setApprovalDateTime(APPROVAL_DATE_TIME);
        List<Participant> participants = newParticipants();
        ScheduledAssociationMeeting scheduledMeeting = new ScheduledAssociationMeeting(COMMUNITY_ID, MEETING_DATE, MEETING_TIME, participants);
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
        MeetingInvite meetingInvite = newMeetingInvite(COMMUNITY_ID, PRESIDENT_ID, TRACKER_ID);
        meetingInvite.setApprovalDateTime(APPROVAL_DATE_TIME);
        List<Participant> participants = newParticipants();
        ScheduledAssociationMeeting scheduledMeeting = new ScheduledAssociationMeeting(COMMUNITY_ID, MEETING_DATE, MEETING_TIME, participants);
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
        MeetingInvite meetingInvite = newMeetingInvite(COMMUNITY_ID, PRESIDENT_ID, TRACKER_ID);
        when(mockMeetingRepository.fetchMeetingInvite(COMMUNITY_ID, TRACKER_ID)).thenReturn(meetingInvite);

        MeetingScheduler scheduler = new MeetingScheduler(mockMeetingRepository, mockNeighbourgRepository, mockMeetingNotificationService, mockTrackerIdRepository, correlationIdLog);
        MeetingInvite result = scheduler.fecthMeetingInvite(COMMUNITY_ID, TRACKER_ID);

        assertThat(result)
                .returns(COMMUNITY_ID, MeetingInvite::getCommunityId)
                .returns(PRESIDENT_ID, MeetingInvite::getApproverId)
                .returns(null, MeetingInvite::getApprovalDateTime)
                .returns(MEETING_DATE, MeetingInvite::getDate)
                .returns(MEETING_TIME, MeetingInvite::getTime)
        ;

    }

    private MeetingInvite newMeetingInvite(CommunityId communityId, NeighbourgId approverId) {
        MeetingInvite meetingInvite = new MeetingInvite(communityId, MEETING_DATE, MEETING_TIME);
        meetingInvite.setApproverId(approverId);
        meetingInvite.setApprovalDateTime(APPROVAL_DATE_TIME);
        return meetingInvite;
    }

    private MeetingInvite newMeetingInvite(CommunityId communityId, NeighbourgId presidentId, TrackerId trackerId) {
        MeetingInvite meetingInvite = newMeetingInvite(communityId, presidentId);
        meetingInvite.setTrackerId(trackerId);
        meetingInvite.setApprovalDateTime(null);
        return meetingInvite;
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
