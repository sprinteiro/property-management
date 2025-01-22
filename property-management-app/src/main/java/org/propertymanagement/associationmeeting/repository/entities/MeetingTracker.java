package org.propertymanagement.associationmeeting.repository.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class MeetingTracker {
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
    private AssociationMeeting meetingId;
}
