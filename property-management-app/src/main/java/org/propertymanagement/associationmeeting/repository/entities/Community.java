package org.propertymanagement.associationmeeting.repository.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Collection;

@Entity
@Data
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMUNITY_ID")
    private Long id;
    private String cif;
    private String address;
    private Long presidentId;
    private Long vicePresidentId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "community")
    private Collection<AssociationMeeting> meetings;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "thecommunity", fetch = FetchType.EAGER)
    private Collection<Neighbour> neighbourgs;
}
