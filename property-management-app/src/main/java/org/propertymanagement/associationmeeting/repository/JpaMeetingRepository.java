package org.propertymanagement.associationmeeting.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.propertymanagement.associationmeeting.exception.MeetingScheduleException;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.AssociationMeetingEntity;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.CommunityEntity;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.MeetingParticipantEntity;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.MeetingTrackerEntity;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.NeighbourEntity;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class JpaMeetingRepository implements MeetingRepository {
    private static final Logger log = LoggerFactory.getLogger(JpaMeetingRepository.class);
    private final EntityManager entityManager;
    private final NeighbourRepository neighbourRepository;

    public JpaMeetingRepository(EntityManager entityManager, NeighbourRepository neighbourRepository) {
        this.entityManager = entityManager;
        this.neighbourRepository = neighbourRepository;
    }

    @Transactional
    @Override
    public void registerMeetingInvite(MeetingInvite newMeetingInvite) {
        CommunityEntity community = entityManager.find(CommunityEntity.class, newMeetingInvite.communityId().value());
        if (isNull(community)) {
            return;
        }

        Long approverId = community.getPresidentId();
        AssociationMeetingEntity associationMeetingEntity = new AssociationMeetingEntity();
        associationMeetingEntity.setApproverId(approverId);
        associationMeetingEntity.setTrackerId(newMeetingInvite.trackerId().toString());
        associationMeetingEntity.setScheduledDate(newMeetingInvite.date().value());
        associationMeetingEntity.setScheduledTime(newMeetingInvite.time().value());
        associationMeetingEntity.setCommunity(community);

        Collection<MeetingParticipantEntity> meetingParticipantEntities = fetchMeetingParticipants(community, associationMeetingEntity);
        associationMeetingEntity.setParticipants(meetingParticipantEntities);

        community.getMeetings().add(associationMeetingEntity);

        entityManager.persist(associationMeetingEntity);
        
        TypedQuery<MeetingTrackerEntity> query = entityManager.createQuery(
                "SELECT mt FROM MeetingTracker mt WHERE mt.trackerId = :trackerId",
                MeetingTrackerEntity.class);
        query.setParameter("trackerId", newMeetingInvite.trackerId().toString());
        MeetingTrackerEntity meetingTrackerEntity = query.getResultStream()
                .findFirst()
                .orElse(null);
        if (meetingTrackerEntity != null) {
            meetingTrackerEntity.setMeetingId(associationMeetingEntity);
            entityManager.persist(meetingTrackerEntity);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public MeetingInvite fetchMeetingInvite(CommunityId communityId, TrackerId trackerId) {
        TypedQuery<AssociationMeetingEntity> query = entityManager.createQuery(
                "SELECT mt FROM MeetingTracker am WHERE mt.trackerId = :trackerId",
                AssociationMeetingEntity.class);
        query.setParameter("trackerId", trackerId.toString());

        return query.getResultStream().findFirst().map(entity ->
        {
            LocalDateTime approvalDateTime = null;
            if (entity.getApprovalDateTime() != null) {
                 approvalDateTime = LocalDateTime.parse(entity.getApprovalDateTime(), DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
            return new MeetingInvite(
                    communityId,
                    new MeetingDate(entity.getScheduledDate()),
                    new MeetingTime(entity.getScheduledTime()),
                    new TrackerId(UUID.fromString(entity.getTrackerId())),
                    new NeighbourgId(entity.getApproverId()),
                    approvalDateTime
            );
        }).orElse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public ScheduledAssociationMeeting fetchScheduledAssociationMeeting(CommunityId communityId, TrackerId trackerId) {
        TypedQuery<AssociationMeetingEntity> query = entityManager.createQuery(
                "SELECT am FROM AssociationMeeting am WHERE am.trackerId = :trackerId",
                AssociationMeetingEntity.class);
        query.setParameter("trackerId", trackerId.toString());

        ScheduledAssociationMeeting persistedScheduledMeeting = query.getResultStream().findFirst().map(entity -> {
                    LocalDateTime approvalDateTime = null;
                    if (entity.getApprovalDateTime() != null) {
                        approvalDateTime = LocalDateTime.parse(entity.getApprovalDateTime(), DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    }
                    return new ScheduledAssociationMeeting(
                            communityId,
                            new TrackerId(UUID.fromString(entity.getTrackerId())),
                            new MeetingDate(entity.getScheduledDate()),
                            new MeetingTime(entity.getScheduledTime()),
                            entity.getParticipants().stream().map(participant ->
                                    new Participant(new NeighbourgId(participant.getId()),
                                            ParticipantRole.getRoleFrom(participant.getParticipantRole()),
                                            null,
                                            null,
                                            null)
                            ).toList(),
                            new NeighbourgId(entity.getApproverId()),
                            approvalDateTime);
        }).orElse(null);

        if (nonNull(persistedScheduledMeeting)) {
            Set<NeighbourgId> participantIds = persistedScheduledMeeting.participants().stream().map(Participant::id).collect(Collectors.toSet());
            List<Participant> participantsWithAllDetails = neighbourRepository.fetchNeighbours(participantIds).stream().toList();
            return new ScheduledAssociationMeeting(
                    persistedScheduledMeeting.communityId(),
                    persistedScheduledMeeting.trackerId(),
                    persistedScheduledMeeting.date(),
                    persistedScheduledMeeting.time(),
                    participantsWithAllDetails,
                    persistedScheduledMeeting.approverId(),
                    persistedScheduledMeeting.approvalDateTime()
            );
        }

        return null;
    }

    @Transactional
    @Override
    public void approveScheduledMeeting(CommunityId communityId, TrackerId trackerId, NeighbourgId approverId) {
        String approvalDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        TypedQuery<AssociationMeetingEntity> query = entityManager.createQuery(
                "SELECT am FROM AssociationMeeting am WHERE am.trackerId = :trackerId",
                AssociationMeetingEntity.class);
        query.setParameter("trackerId", trackerId.toString());
        AssociationMeetingEntity associationMeetingEntity = query.getResultStream().findFirst()
                .map(meeting -> {
                    meeting.setApprovalDateTime(approvalDateTime);
                    return meeting;
                })
                .orElse(null);

        if (isNull(associationMeetingEntity)) {
            log.warn("Approval not found for TrackerId={} CommunityId={}", trackerId, communityId);
            String error = String.format("Unable to approve meeting as not found. TrackerId=%s", trackerId);
            throw new MeetingScheduleException(error, MeetingScheduleException.LogLevel.ERROR);
        }
        entityManager.merge(associationMeetingEntity);
    }
    
    @Transactional
    @Override
    public void approveScheduledMeeting(ScheduledAssociationMeeting scheduledAssociationMeeting) {
        approveScheduledMeeting(scheduledAssociationMeeting.communityId(), scheduledAssociationMeeting.trackerId(), scheduledAssociationMeeting.approverId());
    }

    private Collection<MeetingParticipantEntity> fetchMeetingParticipants(CommunityEntity community, AssociationMeetingEntity meeting) {
        return community.getNeighbourgs().stream().map(neighbour -> {
            MeetingParticipantEntity participant = new MeetingParticipantEntity();
            participant.setId(neighbour.getId());
            participant.setParticipantRole(participantRole(neighbour));
            participant.setMeeting(meeting);
            return participant;
        }).collect(Collectors.toList());
    }

    private String participantRole(NeighbourEntity neighbourg) {
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
