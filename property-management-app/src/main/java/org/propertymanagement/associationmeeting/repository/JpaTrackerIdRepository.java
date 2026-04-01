package org.propertymanagement.associationmeeting.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.propertymanagement.associationmeeting.persistence.jpa.entities.MeetingTrackerEntity;
import org.propertymanagement.domain.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


public class JpaTrackerIdRepository implements TrackerIdRepository {
    private final EntityManager entityManager;

    public JpaTrackerIdRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void register(MeetingInvite meetingInvite) {
        MeetingTrackerEntity entity = new MeetingTrackerEntity();
        entity.setTrackerId(meetingInvite.trackerId().toString());
        entity.setCommunityId(meetingInvite.communityId().value());
        entity.setDate(meetingInvite.date().value());
        entity.setTime(meetingInvite.time().value());

        entityManager.persist(entity);
    }

    @Transactional(readOnly = true)
    @Override
    public MeetingInvite fetchMeetingInvite(TrackerId trackerId) {
        TypedQuery<MeetingTrackerEntity> query = entityManager.createQuery(
                "SELECT mt FROM MeetingTracker mt WHERE mt.trackerId = :trackerId", MeetingTrackerEntity.class);
        query.setParameter("trackerId", trackerId.toString());

        return query.getResultStream().findFirst().map(entity ->
        {
            return new MeetingInvite(
                    new CommunityId(entity.getCommunityId()),
                    new MeetingDate(entity.getDate()),
                    new MeetingTime(entity.getTime()),
                    trackerId,
                    null,
                    null,
                    null
            );
        }).orElse(null);
    }
}
