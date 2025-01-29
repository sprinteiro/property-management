package org.propertymanagement.associationmeeting.web.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Represents data needed to approve a registered meeting for a community.
 */
@Data
public class MeetingApprovalRequestDto {
    /**
     * Valid approver identifier for the registered meeting that belongs to a community.
     */
    @Positive(message = "Approver identifier must be provided")
    private Long approverId;
}
