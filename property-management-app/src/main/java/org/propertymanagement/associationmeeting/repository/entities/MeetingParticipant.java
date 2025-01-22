package org.propertymanagement.associationmeeting.repository.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;

@Entity
@Data
public class MeetingParticipant {
    @Id
    @Column(name = "PARTICIPANT_ID")
    private Long id;
    private String participantRole;
    @Id
    @ManyToOne
    @JoinColumn(name = "MEETING_ID")
    private AssociationMeeting meeting;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MeetingParticipant that = (MeetingParticipant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
