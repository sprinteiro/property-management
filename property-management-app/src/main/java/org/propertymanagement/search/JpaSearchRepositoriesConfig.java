package org.propertymanagement.search;

import org.propertymanagement.associationmeeting.persistence.jpa.entities.CommunityEntity;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.NoOpJpaAssociationMeetingEntity;
import org.propertymanagement.domain.CommunityInfo;
import org.propertymanagement.search.mapper.JpaCommunityEntityMapper;
import org.propertymanagement.search.repository.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackageClasses = DataNoOpSearchRepository.class)
@EntityScan(basePackageClasses = { NoOpJpaAssociationMeetingEntity.class, CommunityEntity.class })
public class JpaSearchRepositoriesConfig {

    @Bean
    public SearchRepository<CommunityInfo> jpaSearchRepository(
            DataCommunityRepository communityRepository,
            JpaCommunityEntityMapper jpaCommunityEntityMapper) {
        return new JpaSearchRepository<>(communityRepository, jpaCommunityEntityMapper);
    }

    @Bean
    public PagedSearchRepository<CommunityInfo> jpaPagedSearchRepository(
            DataCommunityRepository communityRepository,
            JpaCommunityEntityMapper jpaCommunityEntityMapper) {
        return new JpaPagedSearchRepository<>(communityRepository, jpaCommunityEntityMapper);
    }

    @Bean
    public JpaCommunityEntityMapper jpaCommunityEntityMapper() {
        return new JpaCommunityEntityMapper();
    }
}
