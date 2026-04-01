package org.propertymanagement.associationmeeting.config;

import jakarta.persistence.EntityManager;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.NoOpJpaAssociationMeetingEntity;
import org.propertymanagement.associationmeeting.repository.*;
import org.propertymanagement.associationmeeting.repository.sd.NoOpSdAssociationMeetingRepositories;
import org.propertymanagement.associationmeeting.repository.sd.SdNeighbourRepository;
import org.propertymanagement.neighbour.repository.NeighbourRepository;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EntityScan(basePackageClasses = { NoOpJpaAssociationMeetingEntity.class })
@EnableJpaRepositories(basePackageClasses = NoOpSdAssociationMeetingRepositories.class)
public class JpaAssociationMeetingRepositoriesConfig {
    @Bean
    public MeetingRepository jpaMeetingRepository(EntityManager entityManager, NeighbourRepository neighbourRepository) {
        return new JpaMeetingRepository(entityManager, neighbourRepository);
    }

    @Bean
    public NeighbourRepository jpaNeighbourRepository(SdNeighbourRepository sdNeighbourRepository) {
        return new JpaNeighbourRepository(sdNeighbourRepository);
    }

    @Bean
    public TrackerIdRepository jpaTrackerIdRepository(EntityManager entityManager) {
        return new JpaTrackerIdRepository(entityManager);
    }
}
