package org.propertymanagement.associationmeeting.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MeetingParticipantKey implements Serializable {
    @Column(name = "PARTICIPANT_ID")
    private Long participantId;
    @Column(name = "MEETING_ID", insertable=false, updatable=false)
    private Long meetingId;

    public MeetingParticipantKey() {}

    public MeetingParticipantKey(Long participantId, Long meetingId) {
        this.participantId = participantId;
        this.meetingId = meetingId;
    }

    public Long getParticipantId() { return participantId; }
    public void setParticipantId(Long participantId) { this.participantId = participantId; }

    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingParticipantKey that = (MeetingParticipantKey) o;
        return Objects.equals(participantId, that.participantId) && Objects.equals(meetingId, that.meetingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(participantId, meetingId);
    }
}
