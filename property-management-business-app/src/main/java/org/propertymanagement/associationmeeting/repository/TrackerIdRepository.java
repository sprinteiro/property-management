package org.propertymanagement.associationmeeting.repository;

import org.propertymanagement.domain.MeetingInvite;
import org.propertymanagement.domain.TrackerId;

public interface TrackerIdRepository {
    void register(MeetingInvite meetingInvite);
    MeetingInvite fetchMeetingInvite(TrackerId trackerId);
}
