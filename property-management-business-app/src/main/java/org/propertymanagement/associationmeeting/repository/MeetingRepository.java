package org.propertymanagement.associationmeeting.repository;

import org.propertymanagement.domain.*;

public interface MeetingRepository {
    void registerMeetingInvite(MeetingInvite newMeetingInvite);
    MeetingInvite fetchMeetingInvite(CommunityId communityId, TrackerId trackerId);
    ScheduledAssociationMeeting fetchScheduledAssociationMeeting(CommunityId communityId, TrackerId trackerId);
    void approveScheduledMeeting(CommunityId communityId, TrackerId trackerId, NeighbourgId approverId);
}
