package org.propertymanagement.associationmeeting.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents a meeting status.
 */
@JsonInclude(Include.NON_NULL)
public record MeetingStatusDto(
    String date,
    String time,
    TrackingStatus status,
    String approvalDateTime,
    String description,
    String trackerId
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String date;
        private String time;
        private TrackingStatus status;
        private String approvalDateTime;
        private String description;
        private String trackerId;

        public Builder date(String date) {
            this.date = date;
            return this;
        }

        public Builder time(String time) {
            this.time = time;
            return this;
        }

        public Builder status(TrackingStatus status) {
            this.status = status;
            return this;
        }

        public Builder approvalDateTime(String approvalDateTime) {
            this.approvalDateTime = approvalDateTime;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder trackerId(String trackerId) {
            this.trackerId = trackerId;
            return this;
        }

        public MeetingStatusDto build() {
            return new MeetingStatusDto(date, time, status, approvalDateTime, description, trackerId);
        }
    }

    public enum TrackingStatus {
        MEETING_SCHEDULE_REQUESTED,
        MEETING_SCHEDULE_APPROVED
    }
}
