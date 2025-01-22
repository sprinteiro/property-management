package org.propertymanagement.associationmeeting.repository.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Neighbour {
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
    private Community thecommunity;
}
