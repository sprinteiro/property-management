package org.propertymanagement.associationmeeting.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Collection;

@Entity(name = "AssociationMeeting")
@Table(name = "association_meeting")
@Data
public class AssociationMeetingEntity {
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
    private CommunityEntity community;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "meeting")
    private Collection<MeetingParticipantEntity> participants;
}
