package org.propertymanagement.search.repository;

import lombok.RequiredArgsConstructor;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.CommunityEntity;
import org.propertymanagement.domain.CommunityInfo;
import org.propertymanagement.domain.search.OrderedBy;
import org.propertymanagement.domain.search.PagedSearch;
import org.propertymanagement.domain.search.SearchCriteria;
import org.propertymanagement.search.mapper.JpaCommunityEntityMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class JpaPagedSearchRepository<D> implements PagedSearchRepository<D> {
    private final DataCommunityRepository communityRepository;
    private final JpaCommunityEntityMapper jpaCommunityEntityMapper;


    @Override
    public PagedSearch<CommunityInfo> fetchAllCommunitiesInPages(SearchCriteria<D> searchCriteria) {
        if (CommunityInfo.class.getTypeName().equals(searchCriteria.clazz().getTypeName())) {
            Optional<Sort> sort = sortFrom(searchCriteria);

            int pageNumber = Optional.ofNullable(searchCriteria.page().pageNumber()).orElse(0);
            int pageSize = searchCriteria.page().pageSize();

            PageRequest pageRequest = sort
                    .map(it -> PageRequest.of(pageNumber, pageSize, it))
                    .orElse(PageRequest.of(pageNumber, pageSize));


            Page<CommunityEntity> page = communityRepository.findAll(pageRequest);
            List<CommunityInfo> items = jpaCommunityEntityMapper.mapToDomain(page.getContent());
            return new PagedSearch<>(items, page.getTotalElements(), page.getTotalPages(), page.getNumber());
        }

        throw new IllegalArgumentException("Unsupported " + searchCriteria.clazz().getSimpleName());
    }

    private Optional<Sort> sortFrom(SearchCriteria<D> searchCriteria) {
        if (!searchCriteria.orderBy().fields().isEmpty()) {
            String[] fields = searchCriteria
                    .orderBy().fields().stream()
                    .map(domainField -> jpaCommunityEntityMapper.toEntityField(domainField.value()))
                    .toArray(String[]::new);
            return Optional.of(
                    Sort.by(
                            searchCriteria.orderBy().order() == OrderedBy.SearchOrder.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                            fields
                    ));
        }

        return Optional.empty();
    }
}
