package org.propertymanagement.domain;

public class MeetingInvite {
    private MeetingDate date;
    private MeetingTime time;
    private CommunityId communityId;
    private NeighbourgId approverId;
    private String approvalDateTime;
    private TrackerId trackerId;
    private byte[] correlationId;


    public MeetingInvite(CommunityId communityId) {
        this.communityId = communityId;
    }

    public MeetingInvite(CommunityId communityId, MeetingDate date, MeetingTime time) {
        this(communityId);
        this.date = date;
        this.time = time;
    }

    public MeetingInvite(MeetingDate date, MeetingTime time, CommunityId communityId, NeighbourgId approverId, String approvalDateTime, TrackerId trackerId, byte[] correlationId) {
        this(communityId, date, time);
        this.approverId = approverId;
        this.approvalDateTime = approvalDateTime;
        this.trackerId = trackerId;
        this.correlationId = correlationId;
    }

    public MeetingInvite(MeetingInvite meetingInvite, byte[] correlationId) {
        this(meetingInvite.getDate(), meetingInvite.getTime(), meetingInvite.getCommunityId(), meetingInvite.getApproverId(), meetingInvite.getApprovalDateTime(), meetingInvite.getTrackerId(), correlationId);
    }

    public MeetingDate getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = new MeetingDate(date);
    }

    public MeetingTime getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = new MeetingTime(time);
    }

    public void setCommunityId(CommunityId communityId) {
        this.communityId=communityId;
    }

    public CommunityId getCommunityId() {
        return communityId;
    }

    public NeighbourgId getApproverId() {
        return approverId;
    }

    public void setApproverId(NeighbourgId approverId) {
        this.approverId = approverId;
    }

    public String getApprovalDateTime() {
        return approvalDateTime;
    }

    public void setApprovalDateTime(String approvalDateTime) {
        this.approvalDateTime = approvalDateTime;
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

    @Override
    public String toString() {
        return "MeetingInvite{" +
                "communityId=" + communityId +
                ", approverId=" + approverId +
                ", trackerId=" + trackerId +
                ", approvalDateTime='" + approvalDateTime + '\'' +
                ", time=" + time +
                ", date=" + date +
                '}';
    }

}
