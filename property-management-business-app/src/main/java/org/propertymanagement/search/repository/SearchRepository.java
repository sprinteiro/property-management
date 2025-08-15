package org.propertymanagement.search.repository;

import org.propertymanagement.domain.CommunityInfo;
import org.propertymanagement.domain.search.SearchCriteria;

import java.util.List;

public interface SearchRepository<D> {
    List<CommunityInfo> fetchAllCommunities(SearchCriteria<D> searchCriteria);
}
