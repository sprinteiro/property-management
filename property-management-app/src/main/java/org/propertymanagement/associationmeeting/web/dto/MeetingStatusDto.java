package org.propertymanagement.associationmeeting.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class MeetingStatusDto {
    private String date;
    private String time;
    private TrackingStatus status;
    private String approvalDateTime;
    private String description;


    public enum TrackingStatus {
        MEETING_SCHEDULE_REQUESTED,
        MEETING_SCHEDULE_APPROVED
    }
}
