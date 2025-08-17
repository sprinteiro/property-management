package org.propertymanagement.search.repository;

import org.junit.jupiter.api.Test;
import org.propertymanagement.domain.CommunityInfo;
import org.propertymanagement.domain.search.*;
import org.propertymanagement.domain.search.OrderedBy.OrderField;
import org.propertymanagement.search.JpaSearchRepositoriesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(
        properties = {
                "debug=false",
                "spring.jpa.show-sql=true",
                "search.page.size=1"
        }
)
@Sql(scripts = {"classpath:/schema.sql", "classpath:/data.sql"})
public class JpaPagedSearchRepositoryTest {
    @Autowired
    private PagedSearchRepository<CommunityInfo> jpaRepository;


    @Test
    void fetchPagedAllCommunities() {
        // Order based on Primary Key (community_id) as not define (NONE)
        List<ExpectedTriple> expected = List.of(
                ExpectedTriple.of("0384858C", "99 Santander Street", 0),
                ExpectedTriple.of("999999A", "1 Canarias Street", 1),
                ExpectedTriple.of("111111A", "112 Delicias Avenue", 2),
                ExpectedTriple.of("222222A", "112 Delicias Avenue", 3)
        );

        expected.forEach((triple) -> {
            SearchCriteria<CommunityInfo> criteria = searchCriteria(null, null, 1, triple.pageNumber);
            PagedSearch<CommunityInfo> result = jpaRepository.fetchAllCommunitiesInPages(criteria);
            assertPageResult(result, triple.pageNumber, result.totalPages(), triple.cif, triple.address, result.totalElements());
        });
    }

    @Test
    void fetchPagedAllCommunitiesOrderByNameAsc() {
        List<ExpectedTriple> expected = List.of(
                ExpectedTriple.of("0384858C", "99 Santander Street", 0),
                ExpectedTriple.of("111111A", "112 Delicias Avenue", 1),
                ExpectedTriple.of("222222A", "112 Delicias Avenue", 2),
                ExpectedTriple.of("999999A", "1 Canarias Street", 3)
        );

        expected.forEach((triple) -> {
            SearchCriteria<CommunityInfo> criteria = searchCriteria(
                    List.of(OrderField.of("name", OrderedBy.SearchOrder.ASC)),
                    null,
                    1,
                    triple.pageNumber
            );
            PagedSearch<CommunityInfo> result = jpaRepository.fetchAllCommunitiesInPages(criteria);
            assertPageResult(result, triple.pageNumber, result.totalPages(), triple.cif, triple.address, result.totalElements());
        });
    }

    @Test
    void fetchPagedAllCommunitiesOrderByNameDesc() {
        List<ExpectedTriple> expected = List.of(
                ExpectedTriple.of("999999A", "1 Canarias Street", 0),
                ExpectedTriple.of("222222A", "112 Delicias Avenue", 1),
                ExpectedTriple.of("111111A", "112 Delicias Avenue", 2),
                ExpectedTriple.of("0384858C", "99 Santander Street", 3)
        );

        expected.forEach((triple) -> {
            SearchCriteria<CommunityInfo> criteria = searchCriteria(
                    List.of(OrderField.of("name", OrderedBy.SearchOrder.DESC)),
                    null,
                    1,
                    triple.pageNumber
            );
            PagedSearch<CommunityInfo> result = jpaRepository.fetchAllCommunitiesInPages(criteria);
            assertPageResult(result, triple.pageNumber, result.totalPages(), triple.cif, triple.address, result.totalElements());
        });
    }

    @Test
    void fetchPagedAllCommunitiesOrderByAddressAndNameDesc() {
        List<ExpectedTriple> expected = List.of(
                ExpectedTriple.of("0384858C", "99 Santander Street", 0),
                ExpectedTriple.of("222222A", "112 Delicias Avenue", 1),
                ExpectedTriple.of("111111A", "112 Delicias Avenue", 2),
                ExpectedTriple.of("999999A", "1 Canarias Street", 3)
        );

        expected.forEach((triple) -> {
            SearchCriteria<CommunityInfo> criteria = searchCriteria(
                    List.of(OrderField.of("address", OrderedBy.SearchOrder.DESC), OrderField.of("name", OrderedBy.SearchOrder.DESC)),
                    null,
                    1,
                    triple.pageNumber
            );
            PagedSearch<CommunityInfo> result = jpaRepository.fetchAllCommunitiesInPages(criteria);
            assertPageResult(result, triple.pageNumber, result.totalPages(), triple.cif, triple.address, result.totalElements());
        });
    }

    @Test
    void fetchPagedCommunitiesOrderedByAddressDescAndNameAsc() {
        List<ExpectedTriple> expected = List.of(
                ExpectedTriple.of("0384858C", "99 Santander Street", 0),
                ExpectedTriple.of("111111A", "112 Delicias Avenue", 1),
                ExpectedTriple.of("222222A", "112 Delicias Avenue", 2),
                ExpectedTriple.of("999999A", "1 Canarias Street", 3)
        );

        expected.forEach((triple) -> {
            SearchCriteria<CommunityInfo> criteria = searchCriteria(
                    List.of(
                            OrderField.of("address", OrderedBy.SearchOrder.DESC),
                            OrderField.of("name", OrderedBy.SearchOrder.ASC)),
                    null,
                    1,
                    triple.pageNumber
            );
            PagedSearch<CommunityInfo> result = jpaRepository.fetchAllCommunitiesInPages(criteria);
            assertPageResult(result, triple.pageNumber, result.totalPages(), triple.cif, triple.address, result.totalElements());
        });
    }

    @Test
    void fetchPagedCommunitiesFilteredByExactOrLikeAddressOrderByNameDesc() {
        var expectedAddress = "112 Delicias Avenue";
        List<ExpectedTriple> expected = List.of(
                ExpectedTriple.of("222222A", expectedAddress, 0),
                ExpectedTriple.of("111111A", expectedAddress, 1)
        );

        // Exact match by address
        expected.forEach((triple) -> {
            SearchCriteria<CommunityInfo> criteria = searchCriteria(
                    List.of(OrderField.of("name", OrderedBy.SearchOrder.DESC)),
                    List.of(SearchFilter.of("address", expectedAddress, SearchFilter.Hint.EXACT_MATCH)),
                    1,
                    triple.pageNumber
            );
            PagedSearch<CommunityInfo> result = jpaRepository.fetchAllCommunitiesInPages(criteria);
            assertPageResult(result, triple.pageNumber, result.totalPages(), triple.cif, triple.address, result.totalElements());
        });

        // Like-matching by address, pageSize=3 and first page to fetch all matches in the first page
        var criteria = searchCriteria(
                List.of(),
                List.of(SearchFilter.of("address", "1", SearchFilter.Hint.LIKE_MATCH)),
                3,
                0
        );
        PagedSearch<CommunityInfo> result = jpaRepository.fetchAllCommunitiesInPages(criteria);
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.items().stream().filter(it -> it.address().name().equals("112 Delicias Avenue" ))
                .count()).isEqualTo(2);
        assertThat(result.items().stream().filter(it -> it.address().name().equals("1 Canarias Street" ))
                .count()).isEqualTo(1);

        // Exact match (default) by name, pageSize=1 as only one expected item
        String expectedCif = "222222A";
        criteria = searchCriteria(
                List.of(),
                List.of(SearchFilter.of("name", expectedCif)),
                1,
                0
        );
        result = jpaRepository.fetchAllCommunitiesInPages(criteria);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.items().stream().findFirst().get().name()).isEqualTo(expectedCif);

        // Exact match (default) by address and name, pageSize=1 as only one expected item
        criteria = searchCriteria(
                List.of(),
                List.of(SearchFilter.of("address", expectedAddress), SearchFilter.of("name", expectedCif)),
                1,
                0
        );
        result = jpaRepository.fetchAllCommunitiesInPages(criteria);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.items().stream().findFirst().get().name()).isEqualTo(expectedCif);
    }

    @Configuration
    @EnableAutoConfiguration
    @Import({JpaSearchRepositoriesConfig.class})
    static class JpaConfiguration {
    }

    private void assertPageResult(PagedSearch<CommunityInfo> actual, int pageNumber, int totalPages, String name, String address, long totalItems) {
        assertThat(actual.totalPages()).isEqualTo(totalPages);
        assertThat(actual.totalElements()).isEqualTo(totalItems);
        assertThat(actual.pageNumber()).isEqualTo(pageNumber);
        assertThat(actual.items().size()).isEqualTo(1);
        Optional.ofNullable(name).ifPresent(it -> assertThat(actual.items().stream().findFirst().get().name()).isEqualTo(it));
        Optional.ofNullable(address).ifPresent(it -> assertThat(actual.items().stream().findFirst().get().address().name()).isEqualTo(it));
    }

    private SearchCriteria<CommunityInfo> searchCriteria(
            List<OrderField> orderFields,
            List<SearchFilter> filterFields,
            int pageSize,
            int pageNumber
    ) {
        if ((Objects.isNull(orderFields) || orderFields.isEmpty()) && (Objects.isNull(filterFields) || (filterFields.isEmpty()))) {
            return new SearchCriteria<>(CommunityInfo.class, pageNumber);
        }

        return new SearchCriteria<>(
                OrderedBy.of(orderFields),
                FilteredBy.of(filterFields),
                CommunityInfo.class,
                SearchCriteriaPage.of(pageSize, pageNumber)
        );
    }

    private record ExpectedTriple(String cif, String address, int pageNumber) {
        public static ExpectedTriple of(String cif, int pageNumber) {
            return new ExpectedTriple(cif, null, pageNumber);
        }

        public static ExpectedTriple of(String cif, String address, int pageNumber) {
            return new ExpectedTriple(cif, address, pageNumber);
        }
    }

    private record FilterField(String name, String value) {
        public static FilterField of(String name, String value) {
            return new FilterField(name, value);
        }
    }
}
