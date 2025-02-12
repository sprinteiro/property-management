package org.propertymanagement.associationmeeting.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.propertymanagement.associationmeeting.config.LogConfig;
import org.propertymanagement.domain.NeighbourgId;
import org.propertymanagement.domain.Participant;
import org.propertymanagement.neighbour.repository.NeighbourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.propertymanagement.domain.Participant.ParticipantRole.PRESIDENT;

@DataJpaTest(properties = {
        "debug=false"
})
public class JpaNeighbourRepositoryTest {
    private static final String APPROVAL_MEETING_DATE = "29/11/2024";
    private static final String APPROVAL_MEETING_TIME = "10:00";
    @Autowired
    private TestEntityManager jpaEntityManager;
    @Autowired
    private NeighbourRepository jpaRepository;


    @Sql(scripts = { "classpath:/schema.sql", "classpath:/data.sql"})
    @Test
    void fetchPresident() {
        NeighbourgId neighbourId = new NeighbourgId(1L);
        var neighbours = jpaRepository.fetchNeighbours(List.of(neighbourId));
        assertEquals(1, neighbours.size());
        Assertions.assertThat(neighbours.iterator().next())
                .returns(PRESIDENT, Participant::role)
                .returns(neighbourId.value(), participant -> participant.id().value());
    }

    @TestConfiguration
    @Import({ LogConfig.class })
    @ComponentScan(basePackages = "org.propertymanagement.associationmeeting.repository")
    static class JpaConfiguration {
    }
}
