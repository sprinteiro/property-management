package org.propertymanagement.associationmeeting.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;

@Entity(name = "MeetingParticipant")
@Table(name = "meeting_participant")
@Data
public class MeetingParticipantEntity {
    @Id
    @Column(name = "PARTICIPANT_ID")
    private Long id;
    private String participantRole;
    @Id
    @ManyToOne
    @JoinColumn(name = "MEETING_ID")
    private AssociationMeetingEntity meeting;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MeetingParticipantEntity that = (MeetingParticipantEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
