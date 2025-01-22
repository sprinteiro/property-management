package org.propertymanagement.associationmeeting.trackerid;

import org.propertymanagement.domain.MeetingInvite;
import org.propertymanagement.domain.TrackerId;

public interface TrackerIdManager {
    TrackerId generateId();
    boolean registerId(MeetingInvite meetingInvite);
    MeetingInvite fetchMeetingInvite(TrackerId trackerId);
}
