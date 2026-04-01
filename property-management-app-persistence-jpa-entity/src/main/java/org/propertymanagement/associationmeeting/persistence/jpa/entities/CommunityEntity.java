package org.propertymanagement.associationmeeting.persistence.jpa.entities;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Entity(name = "Community")
@Table(name = "community")
public class CommunityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMUNITY_ID")
    private Long id;
    private String cif;
    private String address;
    private Long presidentId;
    private Long vicePresidentId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "community")
    private Collection<AssociationMeetingEntity> meetings;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "thecommunity", fetch = FetchType.EAGER)
    private Collection<NeighbourEntity> neighbourgs;

    public CommunityEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Long getPresidentId() { return presidentId; }
    public void setPresidentId(Long presidentId) { this.presidentId = presidentId; }

    public Long getVicePresidentId() { return vicePresidentId; }
    public void setVicePresidentId(Long vicePresidentId) { this.vicePresidentId = vicePresidentId; }

    public Collection<AssociationMeetingEntity> getMeetings() { return meetings; }
    public void setMeetings(Collection<AssociationMeetingEntity> meetings) { this.meetings = meetings; }

    public Collection<NeighbourEntity> getNeighbourgs() { return neighbourgs; }
    public void setNeighbourgs(Collection<NeighbourEntity> neighbourgs) { this.neighbourgs = neighbourgs; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommunityEntity that = (CommunityEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
