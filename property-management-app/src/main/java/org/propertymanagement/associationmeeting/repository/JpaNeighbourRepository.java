package org.propertymanagement.associationmeeting.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.propertymanagement.associationmeeting.repository.entities.Neighbour;
import org.propertymanagement.domain.*;
import org.propertymanagement.neighbour.repository.NeighbourRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.propertymanagement.domain.Participant.ParticipantRole.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaNeighbourRepository implements NeighbourRepository {
    @PersistenceContext
    private final EntityManager entityManager;


    @Transactional(readOnly = true)
    @Override
    public Collection<Participant> fetchNeighbours(Collection<NeighbourgId> neighbourIds) {
        if (isNull(neighbourIds) || neighbourIds.isEmpty()) {
            return Set.of();
        }

        TypedQuery<Neighbour> query = entityManager.createQuery(
                "SELECT ne FROM Neighbour ne WHERE ne.id IN ( :neighbourIds )",
                Neighbour.class);
        query.setParameter("neighbourIds", neighbourIds.stream().map(NeighbourgId::value).collect(Collectors.toSet()));

        return query.getResultStream().map(entity ->
                new Participant(
                        new NeighbourgId(entity.getId()),
                        participantRole(entity),
                        new Name(entity.getFullname()),
                        new PhoneNumber(entity.getPhonenumber()),
                        new Email(entity.getEmail()))
        ).collect(Collectors.toSet());
    }

    private Participant.ParticipantRole participantRole(Neighbour neighbour) {
        if (isNull(neighbour.getPresident()) && isNull(neighbour.getVicepresident())) {
            return COMMUNITY_MEMBER;
        }
        if (neighbour.getPresident()) {
            return PRESIDENT;
        }
        return VICEPRESIDENT;
    }
}
