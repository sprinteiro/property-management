package org.propertymanagement.domain;

import java.time.LocalDateTime;

/**
 * Represents an invitation for a community meeting.
 * This is a domain entity and acts as an aggregate root for the meeting scheduling process.
 * It is an immutable record, ensuring that its state is always consistent. State transitions
 * are handled by methods that return a new instance of the record.
 */
public record MeetingInvite(
    CommunityId communityId,
    MeetingDate date,
    MeetingTime time,
    TrackerId trackerId,
    NeighbourgId approverId,
    LocalDateTime approvalDateTime
) {

    /**
     * Factory method to create a new meeting invite in its initial state.
     * The invite is not yet tracked or approved.
     */
    public static MeetingInvite create(CommunityId communityId, MeetingDate date, MeetingTime time, NeighbourgId approverId) {
        return new MeetingInvite(communityId, date, time, null, approverId, null);
    }

    /**
     * Returns a new MeetingInvite instance with the tracker ID.
     * This represents the "tracking" state transition.
     */
    public MeetingInvite withTracker(TrackerId newTrackerId) {
        return new MeetingInvite(this.communityId, this.date, this.time, newTrackerId, this.approverId, this.approvalDateTime);
    }

    /**
     * Returns a new, approved MeetingInvite instance.
     * This represents the "approval" state transition and enforces approval rules.
     *
     * @throws IllegalArgumentException if the approver is not the designated one.
     * @throws IllegalStateException if the invite has already been approved.
     */
    public MeetingInvite approve(NeighbourgId approver, LocalDateTime approvalTime) {
        if (this.approverId != null && !this.approverId.equals(approver)) {
            throw new IllegalArgumentException("Invalid approver for this meeting invite.");
        }
        if (this.approvalDateTime != null) {
            throw new IllegalStateException("Meeting invite has already been approved on " + this.approvalDateTime);
        }
        return new MeetingInvite(this.communityId, this.date, this.time, this.trackerId, approver, approvalTime);
    }

    @Override
    public String toString() {
        return "MeetingInvite{" +
                "communityId=" + communityId +
                ", date=" + date +
                ", time=" + time +
                ", trackerId=" + trackerId +
                ", approverId=" + approverId +
                ", approvalDateTime=" + approvalDateTime +
                '}';
    }

    // Records provide default equals and hashCode, which are sufficient now that byte[] is removed.
}
