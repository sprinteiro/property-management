package org.propertymanagement.associationmeeting.config;

import jakarta.persistence.EntityManager;
import org.propertymanagement.associationmeeting.repository.*;
import org.propertymanagement.associationmeeting.repository.entities.NoOpEntity;
import org.propertymanagement.neighbour.repository.NeighbourRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


@EntityScan(basePackageClasses = { NoOpEntity.class })
@ComponentScan(basePackageClasses = NoOpJpaRepositories.class)
public class JpaRepositoriesConfig {
    @Bean
    public MeetingRepository jpaMeetingRepository(EntityManager entityManager, NeighbourRepository neighbourRepository) {
        return new JpaMeetingRepository(entityManager, neighbourRepository);
    }

    @Bean
    public NeighbourRepository jpaNeighbourRepository(EntityManager entityManager) {
        return new JpaNeighbourRepository(entityManager);
    }

    @Bean
    public TrackerIdRepository jpaTrackerIdRepository(EntityManager entityManager) {
        return new JpaTrackerIdRepository(entityManager);
    }
}
