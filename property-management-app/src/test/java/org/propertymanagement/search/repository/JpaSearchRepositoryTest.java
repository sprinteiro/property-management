package org.propertymanagement.search.repository;

import org.junit.jupiter.api.Test;
import org.propertymanagement.associationmeeting.config.JpaAssociationMeetingRepositoriesConfig;
import org.propertymanagement.domain.CommunityInfo;
import org.propertymanagement.domain.search.SearchCriteria;
import org.propertymanagement.search.JpaSearchRepositoriesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(
        properties = {
                "debug=false",
                "spring.jpa.show-sql=true",
                "search.page.size=1"
        }
)
@Sql(scripts = { "classpath:/schema.sql", "classpath:/data.sql"})
public class JpaSearchRepositoryTest {
    @Autowired
    private SearchRepository<CommunityInfo> jpaRepository;

    @Test
    void context() {
        System.out.printf("the context");
    }

    @Test
    void fetchAllCommunities() {
        SearchCriteria<CommunityInfo> criteria = new SearchCriteria<>(CommunityInfo.class);
        Collection<CommunityInfo> result = jpaRepository.fetchAllCommunities(criteria);
        // TODO: JJ - Use test entity manager to populate the expected data and assert
        assertThat(result)
                .isNotNull()
                .hasSize(2);
    }

    @Configuration
    @EnableAutoConfiguration
    @Import({ JpaSearchRepositoriesConfig.class })
    static class JpaConfiguration {
    }
}
