package org.propertymanagement.associationmeeting.trackerid;

import org.propertymanagement.associationmeeting.repository.TrackerIdRepository;
import org.propertymanagement.domain.MeetingInvite;
import org.propertymanagement.domain.TrackerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;


public class DefaultTrackerIdManager implements TrackerIdManager {
    private static final Logger log = LoggerFactory.getLogger(DefaultTrackerIdManager.class);
    private final TrackerIdRepository repository;

    public DefaultTrackerIdManager(TrackerIdRepository repository) {
        this.repository = repository;
    }

    @Override
    public TrackerId generateId() {
        return new TrackerId(UUID.randomUUID());
    }

    @Override
    public boolean registerId(MeetingInvite meetingInvite) {
        try {
            repository.register(meetingInvite);
            log.info("Successfully registered TrackerId={}", meetingInvite.trackerId().toString());
            return true;
        } catch (Exception e) {
            log.error("Error in registering trackerId={} Reason={}", meetingInvite.trackerId().toString(), e.getMessage());
            return false;
        }
    }

    @Override
    public MeetingInvite fetchMeetingInvite(TrackerId trackerId) {
        return repository.fetchMeetingInvite(trackerId);
    }
}
