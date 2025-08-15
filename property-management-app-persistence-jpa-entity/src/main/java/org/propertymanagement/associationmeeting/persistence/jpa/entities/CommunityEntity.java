package org.propertymanagement.associationmeeting.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Collection;

@Entity(name = "Community")
@Table(name = "community")
@Data
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
}
