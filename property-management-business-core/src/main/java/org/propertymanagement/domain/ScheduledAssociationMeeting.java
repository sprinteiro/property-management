package org.propertymanagement.domain;

import java.util.List;

public record ScheduledAssociationMeeting(CommunityId communityId, MeetingDate date, MeetingTime time, List<Participant> participants, byte[] correlationId) {
    public ScheduledAssociationMeeting(ScheduledAssociationMeeting scheduledAssociationMeeting, byte[] correlationId) {
        this(scheduledAssociationMeeting.communityId(), scheduledAssociationMeeting.date(), scheduledAssociationMeeting.time(), scheduledAssociationMeeting.participants(), correlationId);
    }

    public ScheduledAssociationMeeting(CommunityId communityId, MeetingDate date, MeetingTime time, List<Participant> participants) {
        this(communityId, date, time, participants, null);
    }
}
