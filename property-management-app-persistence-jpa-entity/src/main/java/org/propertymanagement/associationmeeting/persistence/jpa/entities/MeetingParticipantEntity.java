package org.propertymanagement.associationmeeting.persistence.jpa.entities;

import jakarta.persistence.*;
import java.util.Objects;

@Entity(name = "MeetingParticipant")
@Table(name = "meeting_participant")
public class MeetingParticipantEntity {
    @Id
    @Column(name = "PARTICIPANT_ID")
    private Long id;
    private String participantRole;
    @Id
    @ManyToOne
    @JoinColumn(name = "MEETING_ID")
    private AssociationMeetingEntity meeting;

    public MeetingParticipantEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getParticipantRole() { return participantRole; }
    public void setParticipantRole(String participantRole) { this.participantRole = participantRole; }

    public AssociationMeetingEntity getMeeting() { return meeting; }
    public void setMeeting(AssociationMeetingEntity meeting) { this.meeting = meeting; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingParticipantEntity that = (MeetingParticipantEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(meeting, that.meeting);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, meeting);
    }
}
