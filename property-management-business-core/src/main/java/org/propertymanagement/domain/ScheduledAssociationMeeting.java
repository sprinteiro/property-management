package org.propertymanagement.domain;

import java.time.LocalDateTime;
import java.util.List;

public record ScheduledAssociationMeeting(
    CommunityId communityId,
    TrackerId trackerId,
    MeetingDate date,
    MeetingTime time,
    List<Participant> participants,
    NeighbourgId approverId,
    LocalDateTime approvalDateTime,
    byte[] correlationId
) {

    public ScheduledAssociationMeeting approve(NeighbourgId approverId, LocalDateTime approvalDateTime) {
        return new ScheduledAssociationMeeting(
            this.communityId,
            this.trackerId,
            this.date,
            this.time,
            this.participants,
            approverId,
            approvalDateTime,
            this.correlationId
        );
    }

    public ScheduledAssociationMeeting withCorrelationId(byte[] newCorrelationId) {
        return new ScheduledAssociationMeeting(
            this.communityId,
            this.trackerId,
            this.date,
            this.time,
            this.participants,
            this.approverId,
            this.approvalDateTime,
            newCorrelationId
        );
    }
    
    // Legacy constructor support if needed, or update callers.
    // For now, I will not include legacy constructors to force updating usages for correctness.
}
