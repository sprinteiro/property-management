package org.propertymanagement.notification.web.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.propertymanagement.associationmeeting.MeetingScheduler;
import org.propertymanagement.domain.*;
import org.propertymanagement.domain.Participant.ParticipantRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * Endpoints for testing purposes
 */
@RestController
@RequestMapping(path = "/test/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = NotificationController.NOTIFICATION_CONTROLLER_TAG, description = "API for testing send notifications")
@Hidden
public class NotificationController {
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    public static final String NOTIFICATION_CONTROLLER_TAG = "NotificationController";
    private final MeetingScheduler meetingScheduler;

    public NotificationController(MeetingScheduler meetingScheduler) {
        this.meetingScheduler = meetingScheduler;
    }

    @GetMapping(path = "/lookup")
    public String lookup() {
        return "Lookup successful!";
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotificationResponseDto> sendNotification(@Valid @RequestBody NotificationRequestDto request) {
        log.info("Test - Received new notification to be sent. Request={}", request);
        ScheduledAssociationMeeting scheduledMeeting = new ScheduledAssociationMeeting(
                new CommunityId(request.getCommunityId()),
                null, // TrackerId not needed for test
                new MeetingDate(request.getDate()),
                new MeetingTime(request.getTime()),
                request.getRecipientIds().stream().map(id ->
                        {
                            Integer recipientId = Math.toIntExact(id);
                            return switch (recipientId) {
                                 case 1 ->
                                         new Participant(
                                                 new NeighbourgId(id),
                                                 ParticipantRole.PRESIDENT,
                                                 new Name("Mr president"),
                                                 new PhoneNumber("+11111111111"),
                                                 new Email("president@test.com"));
                                 case 2 -> new Participant(
                                         new NeighbourgId(id),
                                         ParticipantRole.VICEPRESIDENT,
                                         new Name("Mr vicepresident"),
                                         new PhoneNumber("+22222222222"),
                                         new Email("vicepresident@test.com"));
                                 default -> new Participant(
                                         new NeighbourgId(id),
                                         ParticipantRole.COMMUNITY_MEMBER,
                                         new Name("Member " + recipientId),
                                         new PhoneNumber(recipientId + "0000000000"),
                                         new Email("member" + recipientId + "@test.com"));

                             };
                        }).toList(),
                null, // ApproverId not needed for test
                null // ApprovalDateTime not needed for test
        );
        meetingScheduler.notifyParticipants(scheduledMeeting);

        return ResponseEntity
                .ok(NotificationResponseDto.builder()
                        .description("Notification sent").build()
                );
    }

    public static class NotificationRequestDto {
        @NotNull(message = "Notification type must be provided")
        private String notificationType;
        @NotBlank(message = "Notification date must be provided")
        private String date;
        @NotBlank(message = "Notification time must be provided")
        private String time;
        @Positive(message = "Community identifier must be provided")
        private Long communityId;
        @NotEmpty(message = "A list of recipient identifiers must be provided")
        private List<Long> recipientIds;

        public NotificationRequestDto() {}

        public NotificationRequestDto(String notificationType, String date, String time, Long communityId, List<Long> recipientIds) {
            this.notificationType = notificationType;
            this.date = date;
            this.time = time;
            this.communityId = communityId;
            this.recipientIds = recipientIds;
        }

        public String getNotificationType() { return notificationType; }
        public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public Long getCommunityId() { return communityId; }
        public void setCommunityId(Long communityId) { this.communityId = communityId; }

        public List<Long> getRecipientIds() { return recipientIds; }
        public void setRecipientIds(List<Long> recipientIds) { this.recipientIds = recipientIds; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NotificationRequestDto that = (NotificationRequestDto) o;
            return Objects.equals(notificationType, that.notificationType) && Objects.equals(date, that.date) && Objects.equals(time, that.time) && Objects.equals(communityId, that.communityId) && Objects.equals(recipientIds, that.recipientIds);
        }

        @Override
        public int hashCode() {
            return Objects.hash(notificationType, date, time, communityId, recipientIds);
        }
    }

    public static class NotificationResponseDto {
        private String description;

        public NotificationResponseDto() {}

        public NotificationResponseDto(String description) {
            this.description = description;
        }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String description;

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public NotificationResponseDto build() {
                return new NotificationResponseDto(description);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NotificationResponseDto that = (NotificationResponseDto) o;
            return Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(description);
        }
    }

}
