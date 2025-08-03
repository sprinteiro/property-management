package org.propertymanagement.associationmeeting.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.propertymanagement.associationmeeting.repository.entities.MeetingTracker;
import org.propertymanagement.domain.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
public class JpaTrackerIdRepository implements TrackerIdRepository {
    private final EntityManager entityManager;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void register(MeetingInvite meetingInvite) {
        MeetingTracker entity = new MeetingTracker();
        entity.setTrackerId(meetingInvite.toString());
        entity.setTrackerId(meetingInvite.getTrackerId().toString());
        entity.setCommunityId(meetingInvite.getCommunityId().value());
        entity.setDate(meetingInvite.getDate().value());
        entity.setTime(meetingInvite.getTime().value());

        entityManager.persist(entity);
    }

    @Transactional(readOnly = true)
    @Override
    public MeetingInvite fetchMeetingInvite(TrackerId trackerId) {
        TypedQuery<MeetingTracker> query = entityManager.createQuery(
                "SELECT mt FROM MeetingTracker mt WHERE mt.trackerId = :trackerId", MeetingTracker.class);
        query.setParameter("trackerId", trackerId.toString());

        return query.getResultStream().findFirst().map(entity ->
        {
            MeetingInvite invite = new MeetingInvite(
                    new CommunityId(entity.getCommunityId()),
                    new MeetingDate(entity.getDate()),
                    new MeetingTime(entity.getTime())
            );
            invite.setTrackerId(trackerId);
            return invite;
        }).orElse(null);
    }
}
