package org.propertymanagement.associationmeeting.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.propertymanagement.associationmeeting.config.JpaRepositoriesConfig;
import org.propertymanagement.associationmeeting.repository.entities.AssociationMeeting;
import org.propertymanagement.associationmeeting.repository.entities.MeetingTracker;
import org.propertymanagement.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


@DataJpaTest(properties = {
        "debug=false"
})
public class JpaMeetingRepositoryTest {
    private static final CommunityId COMMUNITY_ID = new CommunityId(1L);
    private static final NeighbourgId PRESIDENT_ID = new NeighbourgId(2L);
    private static final MeetingDate MEETING_DATE = new MeetingDate("01/12/2024");
    private static final MeetingTime MEETING_TIME = new MeetingTime("19:00");
    private static final String APPROVAL_MEETING_DATE = "29/11/2024";
    private static final String APPROVAL_MEETING_TIME = "10:00";
    private static final String APPROVAL_DATE_TIME = APPROVAL_MEETING_DATE + " " + APPROVAL_MEETING_TIME;
    private static final TrackerId TRACKER_ID = new TrackerId(UUID.randomUUID());
    @Autowired
    private TestEntityManager jpaEntityManager;
    @Autowired
    private MeetingRepository jpaRepository;


    @Sql(scripts = { "classpath:/schema.sql", "classpath:/data.sql"})
    @Test
    void registerFetchAndApproveMeetingInvite() {
        // Initialize with trackerId for the meeting invite to be registered
        MeetingTracker meetingTracker = new MeetingTracker();
        meetingTracker.setTrackerId(TRACKER_ID.toString());
        meetingTracker.setCommunityId(COMMUNITY_ID.value());
        meetingTracker.setDate(MEETING_DATE.value());
        meetingTracker.setTime(MEETING_TIME.value());
        jpaEntityManager.persist(meetingTracker);
        jpaEntityManager.flush();

        // Register a new meeting invite
        MeetingInvite meetingInvite =
                new MeetingInvite(MEETING_DATE, MEETING_TIME, COMMUNITY_ID, PRESIDENT_ID, APPROVAL_DATE_TIME, TRACKER_ID, "correlationId".getBytes(UTF_8));
        jpaRepository.registerMeetingInvite(meetingInvite);

        // Fetch the registered meeting invite
        MeetingInvite registeredMeetingInvite = jpaRepository.fetchMeetingInvite(COMMUNITY_ID, meetingInvite.getTrackerId());
        Assertions.assertThat(registeredMeetingInvite)
                .returns(TRACKER_ID.toString(), invite -> invite.getTrackerId().toString())
                .returns(null, MeetingInvite::getApprovalDateTime)
                .returns(MEETING_DATE.value(), invite -> invite.getDate().value())
                .returns(MEETING_TIME.value(), invite -> invite.getTime().value());

        // Fetch the scheduled meeting
        ScheduledAssociationMeeting scheduledAssociationMeeting = jpaRepository.fetchScheduledAssociationMeeting(COMMUNITY_ID, meetingInvite.getTrackerId());
        Assertions.assertThat(scheduledAssociationMeeting)
                .returns(COMMUNITY_ID.value(), meeting -> meeting.communityId().value())
                .returns(MEETING_DATE.value(), meeting -> meeting.date().value())
                .returns(MEETING_TIME.value(), meeting -> meeting.time().value())
                .returns(4, meeting -> meeting.participants().size());

        // Approve the scheduled meeting
        AssociationMeeting associationMeeting = jpaEntityManager.find(AssociationMeeting.class, 1L);
        assertNull(associationMeeting.getApprovalDateTime());
        jpaRepository.approveScheduledMeeting(COMMUNITY_ID, TRACKER_ID, meetingInvite.getApproverId());
        associationMeeting = jpaEntityManager.find(AssociationMeeting.class, 1L);
        assertNotNull(associationMeeting.getApprovalDateTime());
    }

    @Configuration
    @EnableAutoConfiguration
    @Import({ JpaRepositoriesConfig.class })
    static class JpaConfiguration {
    }
}
