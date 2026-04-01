package org.propertymanagement.associationmeeting.persistence.jpa.entities;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Entity(name = "AssociationMeeting")
@Table(name = "association_meeting")
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
    private String correlationId;

    public AssociationMeetingEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }

    public String getApprovalDateTime() { return approvalDateTime; }
    public void setApprovalDateTime(String approvalDateTime) { this.approvalDateTime = approvalDateTime; }

    public String getTrackerId() { return trackerId; }
    public void setTrackerId(String trackerId) { this.trackerId = trackerId; }

    public CommunityEntity getCommunity() { return community; }
    public void setCommunity(CommunityEntity community) { this.community = community; }

    public Collection<MeetingParticipantEntity> getParticipants() { return participants; }
    public void setParticipants(Collection<MeetingParticipantEntity> participants) { this.participants = participants; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssociationMeetingEntity that = (AssociationMeetingEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
