package org.propertymanagement.associationmeeting.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a meeting status.
 */
@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class MeetingStatusDto {
    /**
     * The date of the meeting in dd/MM/yyyy format.
     * Example: "01/12/2025"
     */
    private String date;
    /**
     * The time of the meeting in HH:mm format.
     * Example: "19:00"
     */
    private String time;
    /**
     * Current meeting status (MEETING_SCHEDULE_REQUESTED, MEETING_SCHEDULE_APPROVED)
     */
    private TrackingStatus status;
    /**
     * Date and time of when the meeting was approved.
     */
    private String approvalDateTime;
    /**
     * Goal/Description of the meeting.
     */
    private String description;
    /**
     * Tracker identifier of the registered meeting.
     */
    private String trackerId;


    public enum TrackingStatus {
        MEETING_SCHEDULE_REQUESTED,
        MEETING_SCHEDULE_APPROVED
    }
}
