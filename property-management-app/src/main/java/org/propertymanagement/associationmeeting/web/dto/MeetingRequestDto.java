package org.propertymanagement.associationmeeting.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Represents a request to create a new meeting.
 */
@Data
public class MeetingRequestDto {

    /**
     * The date of the meeting in dd/MM/yyyy format.
     * Example: "01/12/2025"
     */
    @NotBlank
    private String date;
    /**
     * The time of the meeting in HH:mm format.
     * Example: "19:00"
     */
    @NotBlank
    private String time;
}
