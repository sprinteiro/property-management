package org.propertymanagement.associationmeeting.web.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class    MeetingApprovalRequestDto {
    @Positive(message = "Approver identifier must be provided")
    private Long approverId;
}
