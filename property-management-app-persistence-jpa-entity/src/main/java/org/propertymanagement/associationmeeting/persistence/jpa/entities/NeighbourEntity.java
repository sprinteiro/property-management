package org.propertymanagement.associationmeeting.persistence.jpa.entities;

import jakarta.persistence.*;
import java.util.Objects;

@Entity(name = "Neighbour")
@Table(name = "neighbour")
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

    public NeighbourEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public Boolean getPresident() { return president; }
    public void setPresident(Boolean president) { this.president = president; }

    public Boolean getVicepresident() { return vicepresident; }
    public void setVicepresident(Boolean vicepresident) { this.vicepresident = vicepresident; }

    public CommunityEntity getThecommunity() { return thecommunity; }
    public void setThecommunity(CommunityEntity thecommunity) { this.thecommunity = thecommunity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeighbourEntity that = (NeighbourEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
