package org.propertymanagement.search.repository;

import org.propertymanagement.associationmeeting.persistence.jpa.entities.CommunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Internal Spring Data repository interface for Community entities.
 * This is an implementation detail of the persistence module and is not exposed to the domain layer.
 */
@Repository
public interface DataCommunityRepository extends JpaRepository<CommunityEntity, Long>, JpaSpecificationExecutor<CommunityEntity> {
}
