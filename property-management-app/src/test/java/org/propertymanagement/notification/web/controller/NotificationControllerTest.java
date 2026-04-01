package org.propertymanagement.notification.web.controller;

import org.junit.jupiter.api.Test;
import org.propertymanagement.associationmeeting.MeetingScheduler;
import org.propertymanagement.associationmeeting.config.LogConfig;
import org.propertymanagement.associationmeeting.config.WebConfig;
import org.propertymanagement.associationmeeting.config.WebSecurityConfig;
import org.propertymanagement.domain.CommunityId;
import org.propertymanagement.domain.MeetingDate;
import org.propertymanagement.domain.MeetingTime;
import org.propertymanagement.domain.ScheduledAssociationMeeting;
import org.propertymanagement.notification.WebNotificationMeetingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.propertymanagement.domain.ResendMeetingInviteRequest.ResendType.TO_PARTICIPANTS;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureWebTestClient
@ActiveProfiles(profiles = {"h2"})
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {NotificationControllerTest.TestConfig.class},
        properties = {
                "logging.level.org.springframework.security=debug",
                "test.endpoints=on"}
)
@ContextConfiguration(classes = {NotificationControllerTest.TestConfig.class})
public class NotificationControllerTest {
    private static final CommunityId COMMUNITY_ID = new CommunityId(1L);
    private static final MeetingDate MEETING_DATE = new MeetingDate("01/12/2024");
    private static final MeetingTime MEETING_TIME = new MeetingTime("19:00");
    
    @Autowired
    private WebTestClient webTestClient;
    
    @MockitoBean
    private MeetingScheduler meetingScheduler;


    @Test
    void lookupOk() {
        webTestClient.get().uri("/test/notifications/lookup")
                .headers(headers -> {
                    headers.setBasicAuth("admin", "admin");
                    headers.setAccept(List.of(APPLICATION_JSON));
                })
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void lookupUnauthorizedAsBadCredentials() {
        webTestClient.get().uri("/test/notifications/lookup")
                .headers(headers -> {
                    headers.setBasicAuth("invalid", "invalid");
                    headers.setAccept(List.of(APPLICATION_JSON));
                })
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void lookupOkEvenMissingAcceptAndContentTypeHeaders() {
        webTestClient.get().uri("/test/notifications/lookup")
                .headers(headers -> headers.setBasicAuth("admin", "admin"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void sendNotificationCreated() {
        var notificationRequest = newNotificationRequestDto();

        webTestClient.post().uri("/test/notifications")
                .headers(headers -> {
                    headers.setBasicAuth("admin", "admin");
                    headers.setContentType(APPLICATION_JSON);
                    headers.setAccept(List.of(APPLICATION_JSON));
                })
                .bodyValue(notificationRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().value("Location", location -> assertThat(location).contains("/correlationId/"))
                .expectBody(NotificationController.NotificationResponseDto.class)
                .value(body -> {
                    assertThat(body.getCorrelationId()).isNotBlank();
                    assertThat(body.getDescription()).isNotBlank();
                });
        
        verify(meetingScheduler).notifyParticipants(any(ScheduledAssociationMeeting.class));
    }

    private NotificationController.NotificationRequestDto newNotificationRequestDto() {
        var notificationRequest = new NotificationController.NotificationRequestDto();
        notificationRequest.setNotificationType(TO_PARTICIPANTS.name());
        notificationRequest.setDate(MEETING_DATE.value());
        notificationRequest.setTime(MEETING_TIME.value());
        notificationRequest.setCommunityId(COMMUNITY_ID.value());
        notificationRequest.setRecipientIds(List.of(1L, 2L, 3L, 4L));
        return notificationRequest;
    }

    @Configuration
    @EnableAutoConfiguration
    @Import(value = {
            WebConfig.class,
            WebNotificationMeetingConfig.class,
            WebSecurityConfig.class,
            LogConfig.class
    })
    static class TestConfig {
    }
}
