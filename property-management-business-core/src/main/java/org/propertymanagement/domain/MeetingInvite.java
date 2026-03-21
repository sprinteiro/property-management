package org.propertymanagement.domain;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

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
    LocalDateTime approvalDateTime,
    byte[] correlationId
) {

    /**
     * Factory method to create a new meeting invite in its initial state.
     * The invite is not yet tracked or approved.
     */
    public static MeetingInvite create(CommunityId communityId, MeetingDate date, MeetingTime time, NeighbourgId approverId) {
        return new MeetingInvite(communityId, date, time, null, approverId, null, null);
    }

    /**
     * Returns a new MeetingInvite instance with the tracker ID and correlation ID.
     * This represents the "tracking" state transition.
     */
    public MeetingInvite withTracker(TrackerId newTrackerId, byte[] newCorrelationId) {
        return new MeetingInvite(this.communityId, this.date, this.time, newTrackerId, this.approverId, this.approvalDateTime, newCorrelationId);
    }

    /**
     * Returns a new, approved MeetingInvite instance.
     * This represents the "approval" state transition and enforces approval rules.
     *
     * @throws IllegalArgumentException if the approver is not the designated one.
     * @throws IllegalStateException if the invite has already been approved.
     */
    public MeetingInvite approve(NeighbourgId approver, LocalDateTime approvalTime, byte[] newCorrelationId) {
        if (this.approverId != null && !this.approverId.equals(approver)) {
            throw new IllegalArgumentException("Invalid approver for this meeting invite.");
        }
        if (this.approvalDateTime != null) {
            throw new IllegalStateException("Meeting invite has already been approved on " + this.approvalDateTime);
        }
        return new MeetingInvite(this.communityId, this.date, this.time, this.trackerId, approver, approvalTime, newCorrelationId);
    }

    /**
     * Returns a new MeetingInvite with an updated correlation ID.
     */
    public MeetingInvite withCorrelationId(byte[] newCorrelationId) {
        return new MeetingInvite(this.communityId, this.date, this.time, this.trackerId, this.approverId, this.approvalDateTime, newCorrelationId);
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
                ", correlationId=" + (correlationId != null ? new String(correlationId) : "null") +
                '}';
    }

    // Records provide default equals and hashCode, but this implementation is needed to correctly handle the byte array.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingInvite that = (MeetingInvite) o;
        return Objects.equals(communityId, that.communityId) &&
               Objects.equals(date, that.date) &&
               Objects.equals(time, that.time) &&
               Objects.equals(trackerId, that.trackerId) &&
               Objects.equals(approverId, that.approverId) &&
               Objects.equals(approvalDateTime, that.approvalDateTime) &&
               Arrays.equals(correlationId, that.correlationId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(communityId, date, time, trackerId, approverId, approvalDateTime);
        result = 31 * result + Arrays.hashCode(correlationId);
        return result;
    }
}
