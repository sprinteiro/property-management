package org.propertymanagement.search.mapper;


import org.propertymanagement.associationmeeting.persistence.jpa.entities.CommunityEntity;
import org.propertymanagement.domain.CommunityInfo;
import org.propertymanagement.domain.CommunityId;
import org.propertymanagement.domain.NeighbourgId;


public class JpaCommunityEntityMapper
        implements JpaEntityDomainMapper<CommunityEntity, CommunityInfo> {
    @Override
    public CommunityInfo toDomain(CommunityEntity entity) {
        return new CommunityInfo(
                new CommunityId(entity.getId()),
                entity.getCif(),
                new NeighbourgId(entity.getPresidentId())
        );
    }

}
