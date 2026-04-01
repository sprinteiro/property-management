package org.propertymanagement.associationmeeting.web.dto;

import jakarta.validation.constraints.Positive;
import java.util.Objects;

/**
 * Represents data needed to approve a registered meeting for a community.
 */
public class MeetingApprovalRequestDto {
    /**
     * Valid approver identifier for the registered meeting that belongs to a community.
     */
    @Positive(message = "Approver identifier must be provided")
    private Long approverId;

    public MeetingApprovalRequestDto() {}

    public MeetingApprovalRequestDto(Long approverId) {
        this.approverId = approverId;
    }

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingApprovalRequestDto that = (MeetingApprovalRequestDto) o;
        return Objects.equals(approverId, that.approverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(approverId);
    }
}
