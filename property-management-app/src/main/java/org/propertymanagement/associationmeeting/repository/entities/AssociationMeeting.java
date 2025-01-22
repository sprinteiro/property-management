package org.propertymanagement.associationmeeting.repository.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Collection;

@Entity
@Data
public class AssociationMeeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEETING_ID")
    private Long id;
    private String scheduledDate;
    private String scheduledTime;
    private Long approverId;
    private String approvalDateTime;
    @Column(unique = true)
    private String trackerId;
    @ManyToOne
    @JoinColumn(name="COMMUNITY_ID", nullable = false)
    private Community community;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "meeting")
    private Collection<MeetingParticipant> participants;
}
