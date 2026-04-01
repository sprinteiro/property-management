package org.propertymanagement.associationmeeting.repository;

import org.propertymanagement.associationmeeting.persistence.jpa.entities.NeighbourEntity;
import org.propertymanagement.associationmeeting.repository.sd.SdNeighbourRepository;
import org.propertymanagement.domain.*;
import org.propertymanagement.neighbour.repository.NeighbourRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.propertymanagement.domain.Participant.ParticipantRole.*;

public class JpaNeighbourRepository implements NeighbourRepository {
    private final SdNeighbourRepository sdNeighbourRepository;

    public JpaNeighbourRepository(SdNeighbourRepository sdNeighbourRepository) {
        this.sdNeighbourRepository = sdNeighbourRepository;
    }


    @Transactional(readOnly = true)
    @Override
    public Collection<Participant> fetchNeighbours(Collection<NeighbourgId> neighbourIds) {
        if (isNull(neighbourIds) || neighbourIds.isEmpty()) {
            return Set.of();
        }

        return sdNeighbourRepository.findNeighbourByIds(neighbourIds.stream().map(NeighbourgId::value).collect(Collectors.toSet()))
                .stream().map(entity ->
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
