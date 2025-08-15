package org.propertymanagement.associationmeeting.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "Neighbour")
@Table(name = "neighbour")
@Data
public class NeighbourEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NEIGHBOURG_ID")
    private Long id;
    private String phonenumber;
    private String email;
    private String fullname;
    private Boolean president;
    private Boolean vicepresident;
    @ManyToOne
    @JoinColumn(name = "COMMUNITY_ID", nullable = false)
    private CommunityEntity thecommunity;
}
