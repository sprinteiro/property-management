package org.propertymanagement.associationmeeting.persistence.jpa.entities;

import jakarta.persistence.*;
import java.util.Objects;

@Entity(name = "MeetingTracker")
@Table(name = "meeting_tracker")
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

    public MeetingTrackerEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTrackerId() { return trackerId; }
    public void setTrackerId(String trackerId) { this.trackerId = trackerId; }

    public Long getCommunityId() { return communityId; }
    public void setCommunityId(Long communityId) { this.communityId = communityId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public AssociationMeetingEntity getMeetingId() { return meetingId; }
    public void setMeetingId(AssociationMeetingEntity meetingId) { this.meetingId = meetingId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingTrackerEntity that = (MeetingTrackerEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
