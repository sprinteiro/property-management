package org.propertymanagement.associationmeeting.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MeetingRequestDto {
    @NotBlank
    private String date;
    @NotBlank
    private String time;
}
