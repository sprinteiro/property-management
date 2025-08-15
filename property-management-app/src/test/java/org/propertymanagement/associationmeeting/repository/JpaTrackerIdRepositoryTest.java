package org.propertymanagement.associationmeeting.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.propertymanagement.associationmeeting.config.JpaAssociationMeetingRepositoriesConfig;
import org.propertymanagement.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

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
    private static final String APPROVAL_DATE_TIME = APPROVAL_MEETING_DATE + " " + APPROVAL_MEETING_TIME;
    private static final TrackerId TRACKER_ID = new TrackerId(UUID.randomUUID());
    @Autowired
    private TestEntityManager jpaEntityManager;
    @Autowired
    private TrackerIdRepository jpaRepository;

    @Sql(scripts = {"classpath:/schema.sql", "classpath:/data.sql"})
    @Test
    void registerTrackerIdAndMeetingInviteAndFetchMeetingInvite() {
        MeetingInvite meetingInvite = new MeetingInvite(MEETING_DATE, MEETING_TIME, COMMUNITY_ID, PRESIDENT_ID, APPROVAL_DATE_TIME, TRACKER_ID, "correlationId".getBytes(UTF_8));
        jpaRepository.register(meetingInvite);

        MeetingInvite registeredMeetingInvite = jpaRepository.fetchMeetingInvite(meetingInvite.getTrackerId());

        Assertions.assertThat(registeredMeetingInvite)
                .returns(TRACKER_ID.toString(), invite -> invite.getTrackerId().toString())
                .returns(null, MeetingInvite::getApprovalDateTime)
                .returns(MEETING_DATE.value(), invite -> invite.getDate().value())
                .returns(MEETING_TIME.value(), invite -> invite.getTime().value());
    }

    @Configuration
    @Import({ JpaAssociationMeetingRepositoriesConfig.class })
    static class JpaConfiguration {
    }
}
