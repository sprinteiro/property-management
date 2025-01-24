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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.propertymanagement.domain.ResendMeetingInviteRequest.ResendType.TO_PARTICIPANTS;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

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
    private TestRestTemplate restTemplate;
    @MockBean
    private MeetingScheduler meetingScheduler;


    @Test
    void lookupOk() {
        var headers = newHttpHeaders("admin", "admin");

        var response = restTemplate.exchange(
                "/test/notifications/lookup",
                GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void lookupUnauthorizedAsBadCredentials() {
        var headers = newHttpHeaders("invalid", "invalid");

        var response = restTemplate.exchange(
                "/test/notifications/lookup",
                GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED);
    }

    @Test
    public void lookupForbiddenAsMissingAcceptAndContenttypeHeaders() {
        var headers = new HttpHeaders();
        // Set the Authorization header with the Base64-encoded credentials for Basic Authentication
        headers.setBasicAuth("admin", "admin");

        var response = restTemplate.exchange(
                "/test/notifications/lookup",
                GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    public void sendNotificationCreated() {
        var headers = newHttpHeaders("admin", "admin");
        var notificationRequest = newNotificationRequestDto();

        var response = restTemplate.exchange(
                "/test/notifications",
                POST,
                new HttpEntity<>(notificationRequest, headers),
                NotificationController.NotificationResponseDto.class
        );
        assertThat(response)
                .returns(HttpStatus.CREATED, from(ResponseEntity::getStatusCode)) // Check status code
                .satisfies(res -> {
                    assertThat(res.getHeaders().getLocation().getPath())
                            .contains("/correlationId/"); // Check location header
                })
                .extracting(ResponseEntity::getBody) // Extract the response body
                .satisfies(body -> {
                    assertThat(body.getCorrelationId()).isNotBlank(); // Check correlationId
                    assertThat(body.getDescription()).isNotBlank(); // Check description
                });
        verify(meetingScheduler).notifyParticipants(any(ScheduledAssociationMeeting.class));
    }

    private HttpHeaders newHttpHeaders(String userName, String password) {
        var headers = new HttpHeaders();
        headers.setAccept(List.of(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        // Set the Authorization header with the Base64-encoded credentials for Basic Authentication
        headers.setBasicAuth(userName, password);
        return headers;
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
