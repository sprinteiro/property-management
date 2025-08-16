package org.propertymanagement.search.mapper;


import org.propertymanagement.associationmeeting.persistence.jpa.entities.CommunityEntity;
import org.propertymanagement.domain.CommunityAddress;
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
                new NeighbourgId(entity.getPresidentId()),
                new CommunityAddress(entity.getAddress())
        );
    }

    @Override
    public String toEntityField(String domainField) {
        if ("name".equals(domainField)) {
            return "cif";
        }
        if ("address".equals(domainField)) {
            return "address";
        }

        throw  new IllegalArgumentException("Unsupported field " + domainField);
    }

}
