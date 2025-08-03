package org.propertymanagement.associationmeeting.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.propertymanagement.associationmeeting.exception.MeetingScheduleException;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.AssociationMeeting;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.Community;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.MeetingParticipant;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.MeetingTracker;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.Neighbour;
import org.propertymanagement.domain.CommunityId;
import org.propertymanagement.domain.MeetingDate;
import org.propertymanagement.domain.MeetingInvite;
import org.propertymanagement.domain.MeetingTime;
import org.propertymanagement.domain.NeighbourgId;
import org.propertymanagement.domain.Participant;
import org.propertymanagement.domain.Participant.ParticipantRole;
import org.propertymanagement.domain.ScheduledAssociationMeeting;
import org.propertymanagement.domain.TrackerId;
import org.propertymanagement.neighbour.repository.NeighbourRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Slf4j
public class JpaMeetingRepository implements MeetingRepository {
    private final EntityManager entityManager;
    private final NeighbourRepository neighbourRepository;

    @Transactional
    @Override
    public void registerMeetingInvite(MeetingInvite newMeetingInvite) {
        Community community = entityManager.find(Community.class, newMeetingInvite.getCommunityId().value());
        if (isNull(community)) {
            return;
        }

        Long approverId = community.getPresidentId();
        AssociationMeeting associationMeeting = new AssociationMeeting();
        associationMeeting.setApproverId(approverId);
        associationMeeting.setTrackerId(newMeetingInvite.getTrackerId().toString());
        associationMeeting.setScheduledDate(newMeetingInvite.getDate().value());
        associationMeeting.setScheduledTime(newMeetingInvite.getTime().value());
        associationMeeting.setCommunity(community);

        Collection<MeetingParticipant> meetingParticipants = fetchMeetingParticipants(community, associationMeeting);
        associationMeeting.setParticipants(meetingParticipants);

        community.getMeetings().add(associationMeeting);

        entityManager.persist(associationMeeting);
        newMeetingInvite.setApproverId(new NeighbourgId(approverId));

        TypedQuery<MeetingTracker> query = entityManager.createQuery(
                "SELECT mt FROM MeetingTracker mt WHERE mt.trackerId = :trackerId",
                MeetingTracker.class);
        query.setParameter("trackerId", newMeetingInvite.getTrackerId().toString());
        MeetingTracker meetingTracker = query.getResultStream()
                .findFirst()
                .orElse(null);
        meetingTracker.setMeetingId(associationMeeting);
        entityManager.persist(meetingTracker);
    }

    @Transactional(readOnly = true)
    @Override
    public MeetingInvite fetchMeetingInvite(CommunityId communityId, TrackerId trackerId) {
        TypedQuery<AssociationMeeting> query = entityManager.createQuery(
                "SELECT am FROM AssociationMeeting am WHERE am.trackerId = :trackerId",
                AssociationMeeting.class);
        query.setParameter("trackerId", trackerId.toString());

        return query.getResultStream().findFirst().map(entity ->
        {
            MeetingInvite invite = new MeetingInvite(communityId, new MeetingDate(entity.getScheduledDate()), new MeetingTime(entity.getScheduledTime()));
            invite.setApproverId(new NeighbourgId(entity.getApproverId()));
            invite.setApprovalDateTime(entity.getApprovalDateTime());
            invite.setTrackerId(new TrackerId(UUID.fromString(entity.getTrackerId())));
            return invite;
        }).orElse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public ScheduledAssociationMeeting fetchScheduledAssociationMeeting(CommunityId communityId, TrackerId trackerId) {
        TypedQuery<AssociationMeeting> query = entityManager.createQuery(
                "SELECT am FROM AssociationMeeting am WHERE am.trackerId = :trackerId",
                AssociationMeeting.class);
        query.setParameter("trackerId", trackerId.toString());

        ScheduledAssociationMeeting persistedScheduledMeeting = query.getResultStream().findFirst().map(entity ->
                        new ScheduledAssociationMeeting(
                                communityId,
                                new MeetingDate(entity.getScheduledDate()),
                                new MeetingTime(entity.getScheduledTime()),
                                entity.getParticipants().stream().map(participant ->
                                        new Participant(new NeighbourgId(participant.getId()),
                                                ParticipantRole.getRoleFrom(participant.getParticipantRole()),
                                                null,
                                                null,
                                                null)
                                ).toList(),
                                null))
                .orElse(null);

        if (nonNull(persistedScheduledMeeting)) {
            Set<NeighbourgId> participantIds = persistedScheduledMeeting.participants().stream().map(Participant::id).collect(Collectors.toSet());
            List<Participant> participantsWithAllDetails = neighbourRepository.fetchNeighbours(participantIds).stream().toList();
            return new ScheduledAssociationMeeting(
                    persistedScheduledMeeting.communityId(),
                    persistedScheduledMeeting.date(),
                    persistedScheduledMeeting.time(),
                    participantsWithAllDetails
            );
        }

        return null;
    }

    @Transactional
    @Override
    public void approveScheduledMeeting(CommunityId communityId, TrackerId trackerId, NeighbourgId approverId) {
        String approvalDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        TypedQuery<AssociationMeeting> query = entityManager.createQuery(
                "SELECT am FROM AssociationMeeting am WHERE am.trackerId = :trackerId",
                AssociationMeeting.class);
        query.setParameter("trackerId", trackerId.toString());
        AssociationMeeting associationMeeting = query.getResultStream().findFirst()
                .map(meeting -> {
                    meeting.setApprovalDateTime(approvalDateTime);
                    return meeting;
                })
                .orElse(null);

        if (isNull(associationMeeting)) {
            log.warn("Approval not found for TrackerId={} CommunityId={}", trackerId, communityId);
            String error = String.format("Unable to approve meeting as not found. TrackerId=%d", trackerId);
            throw new MeetingScheduleException(error, MeetingScheduleException.LogLevel.ERROR);
        }
        entityManager.merge(associationMeeting);
    }

    private Collection<MeetingParticipant> fetchMeetingParticipants(Community community, AssociationMeeting meeting) {
        return community.getNeighbourgs().stream().map(neighbour -> {
            MeetingParticipant participant = new MeetingParticipant();
            participant.setId(neighbour.getId());
            participant.setParticipantRole(participantRole(neighbour));
            participant.setMeeting(meeting);
            return participant;
        }).collect(Collectors.toList());
    }

    private String participantRole(Neighbour neighbourg) {
        if (isNull(neighbourg.getPresident()) && isNull(neighbourg.getVicepresident())) {
            return "community_member";
        }
        if (nonNull(neighbourg.getPresident()) && neighbourg.getPresident()) {
            return "president";
        }
        return "vicepresident";
    }

    private ParticipantRole participantType(String participantRole) {
        if (isNull(participantRole)) {
            return ParticipantRole.COMMUNITY_MEMBER;
        }
        if ("president".equalsIgnoreCase(participantRole)) {
            return ParticipantRole.PRESIDENT;
        } else {
            return ParticipantRole.VICEPRESIDENT;
        }
    }
}
