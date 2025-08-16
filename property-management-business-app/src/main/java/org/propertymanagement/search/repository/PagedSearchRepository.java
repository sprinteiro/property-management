package org.propertymanagement.search.repository;

import org.propertymanagement.domain.CommunityInfo;
import org.propertymanagement.domain.search.PagedSearch;
import org.propertymanagement.domain.search.SearchCriteria;


public interface PagedSearchRepository<D> {
    PagedSearch<CommunityInfo> fetchAllCommunitiesInPages(SearchCriteria<D> searchCriteria);
}
