package org.propertymanagement.associationmeeting.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Objects;

/**
 * Represents a request to resend a meeting invite.
 */
public class ResendInviteDto {
    /**
     * Unique identifier for the community.
     */
    @Positive(message = "Community identifier must be provided")
    private Long communityId;
    /**
     * Unique tracker identifier for the meeting.
     */
    @NotBlank(message = "Tracker identifier must be provided")
    private String trackerId;
    /**
     * The specific action/target for resending the invite.
     */
    @NotNull(message = "Resend action must be provided")
    private RESEND_ACTION action;

    public ResendInviteDto() {}

    public ResendInviteDto(Long communityId, String trackerId, RESEND_ACTION action) {
        this.communityId = communityId;
        this.trackerId = trackerId;
        this.action = action;
    }

    public Long getCommunityId() { return communityId; }
    public void setCommunityId(Long communityId) { this.communityId = communityId; }

    public String getTrackerId() { return trackerId; }
    public void setTrackerId(String trackerId) { this.trackerId = trackerId; }

    public RESEND_ACTION getAction() { return action; }
    public void setAction(RESEND_ACTION action) { this.action = action; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResendInviteDto that = (ResendInviteDto) o;
        return Objects.equals(communityId, that.communityId) && Objects.equals(trackerId, that.trackerId) && action == that.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(communityId, trackerId, action);
    }

    @Override
    public String toString() {
        return "ResendInviteDto{" +
                "communityId=" + communityId +
                ", trackerId='" + trackerId + '\'' +
                ", action=" + action +
                '}';
    }

    public enum RESEND_ACTION {
        FOR_APPROVAL,
        TO_PARTICIPANTS
    }
}
