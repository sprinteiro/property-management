package org.propertymanagement.associationmeeting.trackerid;

import org.junit.jupiter.api.Test;
import org.propertymanagement.associationmeeting.repository.TrackerIdRepository;
import org.propertymanagement.domain.*;

import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DefaultTrackerIdManagerTest {
    private static final CommunityId COMMUNITY_ID = new CommunityId(1L);
    private static final NeighbourgId PRESIDENT_ID = new NeighbourgId(2L);
    private static final MeetingDate MEETING_DATE = new MeetingDate("01/12/2024");
    private static final MeetingTime MEETING_TIME = new MeetingTime("19:00");
    private static final String APPROVAL_MEETING_DATE = "29/11/2024";
    private static final String APPROVAL_MEETING_TIME = "10:00";
    private static final String APPROVAL_DATE_TIME = APPROVAL_MEETING_DATE + " " + APPROVAL_MEETING_TIME;

    private static final TrackerId TRACKER_ID = new TrackerId(UUID.randomUUID());

    @Test
    void registerTrackerId() {
        MeetingInvite meetingInvite = new MeetingInvite(COMMUNITY_ID, MEETING_DATE, MEETING_TIME);
        meetingInvite.setTrackerId(TRACKER_ID);
        meetingInvite.setCorrelationId("correlationId".getBytes(UTF_8));
        TrackerIdRepository mockTrackerIdRepository = mock(TrackerIdRepository.class);

        TrackerIdManager trackerIdManager = new DefaultTrackerIdManager(mockTrackerIdRepository);

        assertTrue(trackerIdManager.registerId(meetingInvite));
        verify(mockTrackerIdRepository).register(meetingInvite);
    }

    @Test
    void unableRegisterTrackerId() {
        MeetingInvite meetingInvite = new MeetingInvite(COMMUNITY_ID, MEETING_DATE, MEETING_TIME);
        meetingInvite.setTrackerId(TRACKER_ID);
        meetingInvite.setCorrelationId("correlationId".getBytes(UTF_8));
        TrackerIdRepository mockTrackerIdRepository = mock(TrackerIdRepository.class);
        doThrow(RuntimeException.class).when(mockTrackerIdRepository).register(meetingInvite);

        TrackerIdManager trackerIdManager = new DefaultTrackerIdManager(mockTrackerIdRepository);

        assertFalse(trackerIdManager.registerId(meetingInvite));
        verify(mockTrackerIdRepository).register(meetingInvite);
    }

    @Test
    void fetchMeetingInvite() {
        MeetingInvite meetingInvite = new MeetingInvite(COMMUNITY_ID);
        TrackerIdRepository mockTrackerIdRepository = mock(TrackerIdRepository.class);
        when(mockTrackerIdRepository.fetchMeetingInvite(TRACKER_ID)).thenReturn(meetingInvite);

        TrackerIdManager trackerIdManager = new DefaultTrackerIdManager(mockTrackerIdRepository);

        MeetingInvite result = trackerIdManager.fetchMeetingInvite(TRACKER_ID);
        assertEquals(COMMUNITY_ID, result.getCommunityId());

        verify(mockTrackerIdRepository).fetchMeetingInvite(TRACKER_ID);
    }

    @Test
    void generateTrackerId() {
        MeetingInvite meetingInvite = new MeetingInvite(COMMUNITY_ID);
        TrackerIdRepository mockTrackerIdRepository = mock(TrackerIdRepository.class);
        when(mockTrackerIdRepository.fetchMeetingInvite(TRACKER_ID)).thenReturn(meetingInvite);

        TrackerIdManager trackerIdManager = new DefaultTrackerIdManager(mockTrackerIdRepository);

        assertNotNull(trackerIdManager.generateId());
    }
}
