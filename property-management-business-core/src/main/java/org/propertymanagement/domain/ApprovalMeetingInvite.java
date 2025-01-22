package org.propertymanagement.domain;

import java.util.Arrays;

public class ApprovalMeetingInvite {
    private CommunityId communityId;
    private NeighbourgId approverId;
    private TrackerId trackerId;
    private byte[] correlationId;
    private MeetingInvite meetingInvite;


    public ApprovalMeetingInvite(CommunityId communityId) {
        this.communityId = communityId;
    }

    public ApprovalMeetingInvite(CommunityId communityId, TrackerId trackerId, NeighbourgId approverId) {
        this(communityId);
        this.trackerId = trackerId;
        this.approverId = approverId;
    }

    public CommunityId getCommunityId() {
        return communityId;
    }

    public void setCommunityId(CommunityId communityId) {
        this.communityId = communityId;
    }

    public NeighbourgId getApproverId() {
        return approverId;
    }

    public void setApproverId(NeighbourgId approverId) {
        this.approverId = approverId;
    }

    public TrackerId getTrackerId() {
        return trackerId;
    }

    public void setTrackerId(TrackerId trackerId) {
        this.trackerId = trackerId;
    }

    public byte[] getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(byte[] correlationId) {
        this.correlationId = correlationId;
    }

    public MeetingInvite getMeetingInvite() {
        return meetingInvite;
    }

    public void setMeetingInvite(MeetingInvite meetingInvite) {
        this.meetingInvite = meetingInvite;
    }

    @Override
    public String toString() {
        return "ApprovalMeetingInvite{" +
                "communityId=" + communityId +
                ", approverId=" + approverId +
                ", trackerId=" + trackerId +
                ", correlationId=" + Arrays.toString(correlationId) +
                ", meetingInvite=" + meetingInvite +
                '}';
    }
}
