package org.propertymanagement.associationmeeting.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "MeetingTracker")
@Table(name = "meeting_tracker")
@Data
public class MeetingTrackerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String trackerId;
    @Column(nullable = false)
    private Long communityId;
    @Column(nullable = false)
    private String date;
    @Column(nullable = false)
    private String time;

    @OneToOne
    @JoinColumn(name = "MEETING_ID")
    private AssociationMeetingEntity meetingId;
}
