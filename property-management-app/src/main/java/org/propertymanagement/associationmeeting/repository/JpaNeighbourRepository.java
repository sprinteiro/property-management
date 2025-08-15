package org.propertymanagement.associationmeeting.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.NeighbourEntity;
import org.propertymanagement.domain.*;
import org.propertymanagement.neighbour.repository.NeighbourRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.propertymanagement.domain.Participant.ParticipantRole.*;

@Slf4j
@RequiredArgsConstructor
public class JpaNeighbourRepository implements NeighbourRepository {
    private final EntityManager entityManager;


    @Transactional(readOnly = true)
    @Override
    public Collection<Participant> fetchNeighbours(Collection<NeighbourgId> neighbourIds) {
        if (isNull(neighbourIds) || neighbourIds.isEmpty()) {
            return Set.of();
        }

        TypedQuery<NeighbourEntity> query = entityManager.createQuery(
                "SELECT ne FROM Neighbour ne WHERE ne.id IN ( :neighbourIds )",
                NeighbourEntity.class);
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

    private Participant.ParticipantRole participantRole(NeighbourEntity neighbour) {
        if (isNull(neighbour.getPresident()) && isNull(neighbour.getVicepresident())) {
            return COMMUNITY_MEMBER;
        }
        if (neighbour.getPresident()) {
            return PRESIDENT;
        }
        return VICEPRESIDENT;
    }
}
