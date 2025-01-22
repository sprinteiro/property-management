package org.propertymanagement.neighbour.repository;

import org.propertymanagement.domain.NeighbourgId;
import org.propertymanagement.domain.Participant;

import java.util.Collection;

public interface NeighbourRepository {
    Collection<Participant> fetchNeighbours(Collection<NeighbourgId> neighbourIds);
}
