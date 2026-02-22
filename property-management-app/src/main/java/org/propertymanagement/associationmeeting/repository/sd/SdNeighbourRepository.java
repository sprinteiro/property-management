package org.propertymanagement.associationmeeting.repository.sd;

import org.propertymanagement.associationmeeting.persistence.jpa.entities.NeighbourEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SdNeighbourRepository extends JpaRepository<NeighbourEntity, Long> {
    @Query("SELECT ne FROM Neighbour ne WHERE ne.id IN ( :ids )")
    List<NeighbourEntity> findNeighbourByIds(Set<Long> ids);
}
