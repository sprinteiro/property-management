package org.propertymanagement.associationmeeting.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class MeetingParticipantKey implements Serializable {
    @Column(name = "PARTICIPANT_ID")
    private Long participantId;
    @Column(name = "MEETING_ID", insertable=false, updatable=false)
    private Long meetingId;
}
