package org.propertymanagement.search.repository;

import org.propertymanagement.associationmeeting.persistence.jpa.entities.CommunityEntity;
import org.propertymanagement.domain.CommunityInfo;
import org.propertymanagement.domain.search.SearchCriteria;
import org.propertymanagement.search.mapper.JpaCommunityEntityMapper;

import java.util.List;

public class JpaSearchRepository<D> implements SearchRepository<D> {
    private final DataCommunityRepository communityRepository;
    private final JpaCommunityEntityMapper jpaCommunityEntityMapper;

    public JpaSearchRepository(DataCommunityRepository communityRepository, JpaCommunityEntityMapper jpaCommunityEntityMapper) {
        this.communityRepository = communityRepository;
        this.jpaCommunityEntityMapper = jpaCommunityEntityMapper;
    }


    @Override
    public List<CommunityInfo> fetchAllCommunities(SearchCriteria<D> searchCriteria) {
        if (CommunityInfo.class.getTypeName().equals(searchCriteria.clazz().getTypeName())) {
            List<CommunityEntity> entities = communityRepository.findAll();
            List<CommunityInfo> items = jpaCommunityEntityMapper.mapToDomain(entities);
            return items;
        }

        throw new IllegalArgumentException("Unsupported " + searchCriteria.clazz().getSimpleName());
    }

}
