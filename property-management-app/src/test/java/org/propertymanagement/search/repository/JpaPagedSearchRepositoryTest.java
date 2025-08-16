package org.propertymanagement.search.repository;

import org.junit.jupiter.api.Test;
import org.propertymanagement.domain.CommunityInfo;
import org.propertymanagement.domain.search.*;
import org.propertymanagement.search.JpaSearchRepositoriesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(
        properties = {
                "debug=false",
                "spring.jpa.show-sql=true",
                "search.page.size=1"
        }
)
@Sql(scripts = { "classpath:/schema.sql", "classpath:/data.sql"})
public class JpaPagedSearchRepositoryTest {
    @Autowired
    private PagedSearchRepository<CommunityInfo> jpaRepository;


    @Test
    void fetchPagedAllCommunities() {
        List<ExpectedPair> expected = List.of(
                ExpectedPair.of("0384858C", 0),
                ExpectedPair.of("999999A", 1),
                ExpectedPair.of("222222A", 2)
        );

        expected.forEach((pair) -> {
            SearchCriteria<CommunityInfo> criteria = searchCriteria(null, null, 1, pair.pageNumber);
            PagedSearch<CommunityInfo> result = jpaRepository.fetchAllCommunitiesInPages(criteria);
            assertPageResult(result, pair.pageNumber, result.totalPages(), pair.cif, result.totalElements());
        });
    }

    @Test
    void fetchPagedAllCommunitiesOrderByNameDesc() {
        List<ExpectedPair> expected = List.of(
                ExpectedPair.of("999999A", 0),
                ExpectedPair.of("222222A", 1),
                ExpectedPair.of("0384858C", 2)
        );

        expected.forEach((pair) -> {
            SearchCriteria<CommunityInfo> criteria = searchCriteria("name", OrderedBy.SearchOrder.DESC, 1, pair.pageNumber);
            PagedSearch<CommunityInfo> result = jpaRepository.fetchAllCommunitiesInPages(criteria);
            assertPageResult(result, pair.pageNumber, result.totalPages(), pair.cif, result.totalElements());
        });
    }

    @Test
    void fetchPagedAllCommunitiesOrderByAddressDesc() {
        List<ExpectedPair> expected = List.of(
                ExpectedPair.of("0384858C", 0),
                ExpectedPair.of("222222A", 1),
                ExpectedPair.of("999999A", 2)
        );

        expected.forEach((pair) -> {
            SearchCriteria<CommunityInfo> criteria = searchCriteria("address", OrderedBy.SearchOrder.DESC, 1, pair.pageNumber);
            PagedSearch<CommunityInfo> result = jpaRepository.fetchAllCommunitiesInPages(criteria);
            assertPageResult(result, pair.pageNumber, result.totalPages(), pair.cif, result.totalElements());
        });
    }

    @Configuration
    @EnableAutoConfiguration
    @Import({ JpaSearchRepositoriesConfig.class })
    static class JpaConfiguration {
    }

    private void assertPageResult(PagedSearch<CommunityInfo> actual, int pageNumber, int totalPages, String item, long totalItems) {
        assertThat(actual.totalPages()).isEqualTo(totalPages);
        assertThat(actual.totalElements()).isEqualTo(totalItems);
        assertThat(actual.pageNumber()).isEqualTo(pageNumber);
        assertThat(actual.items().size()).isEqualTo(1);
        assertThat(actual.items().stream().findFirst().get().name()).isEqualTo(item);
    }

    private SearchCriteria<CommunityInfo> searchCriteria(String field, OrderedBy.SearchOrder orderBy, int pageSize, int pageNumber) {
        if (Objects.isNull(field)) {
            return new SearchCriteria<>(CommunityInfo.class, pageNumber);
        }

        return new SearchCriteria<>(
                new OrderedBy(orderBy, List.of(new FieldName(field))),
                new FilteredBy(List.of()),
                CommunityInfo.class,
                new SearchCriteriaPage(pageSize, pageNumber)
        );
    }

    private record ExpectedPair(String cif, int pageNumber) {
        public static ExpectedPair of(String cif, int pageNumber) {
            return new ExpectedPair(cif, pageNumber);
        }
    }
}
