package org.propertymanagement.search.mapper;

import java.util.List;

public interface JpaEntityDomainMapper<E, D> {
    default List<D> mapToDomain(List<E> jpaEntities) {
        return jpaEntities.stream()
                .map(entity -> toDomain(entity))
                .toList();
    }

    D toDomain(E entity);
    String toEntityField(String domainField);
}
