package org.propertymanagement.associationmeeting.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.propertymanagement.associationmeeting.config.JpaAssociationMeetingRepositoriesConfig;
import org.propertymanagement.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@DataJpaTest(properties = {
        "debug=false"
})
public class JpaTrackerIdRepositoryTest {
    private static final CommunityId COMMUNITY_ID = new CommunityId(1L);
    private static final NeighbourgId PRESIDENT_ID = new NeighbourgId(2L);
    private static final MeetingDate MEETING_DATE = new MeetingDate("01/12/2024");
    private static final MeetingTime MEETING_TIME = new MeetingTime("19:00");
    private static final String APPROVAL_MEETING_DATE = "29/11/2024";
    private static final String APPROVAL_MEETING_TIME = "10:00";
    private static final String APPROVAL_DATE_TIME_STR = APPROVAL_MEETING_DATE + " " + APPROVAL_MEETING_TIME;
    private static final LocalDateTime APPROVAL_DATE_TIME = LocalDateTime.parse(APPROVAL_DATE_TIME_STR, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    private static final TrackerId TRACKER_ID = new TrackerId(UUID.randomUUID());
    @Autowired
    private TestEntityManager jpaEntityManager;
    @Autowired
    private TrackerIdRepository jpaRepository;

    @Sql(scripts = {"classpath:/schema.sql", "classpath:/data.sql"})
    @Test
    void registerTrackerIdAndMeetingInviteAndFetchMeetingInvite() {
        MeetingInvite meetingInvite = new MeetingInvite(COMMUNITY_ID, MEETING_DATE, MEETING_TIME, TRACKER_ID, PRESIDENT_ID, APPROVAL_DATE_TIME);
        jpaRepository.register(meetingInvite);

        MeetingInvite registeredMeetingInvite = jpaRepository.fetchMeetingInvite(meetingInvite.trackerId());

        Assertions.assertThat(registeredMeetingInvite)
                .returns(TRACKER_ID.toString(), invite -> invite.trackerId().toString())
                .returns(null, MeetingInvite::approvalDateTime)
                .returns(MEETING_DATE.value(), invite -> invite.date().value())
                .returns(MEETING_TIME.value(), invite -> invite.time().value());
    }

    @Configuration
    @Import({ JpaAssociationMeetingRepositoriesConfig.class })
    static class JpaConfiguration {
    }
}
