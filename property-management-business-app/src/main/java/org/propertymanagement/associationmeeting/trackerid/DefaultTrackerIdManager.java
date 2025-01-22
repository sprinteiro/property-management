package org.propertymanagement.associationmeeting.trackerid;

import org.propertymanagement.associationmeeting.repository.TrackerIdRepository;
import org.propertymanagement.domain.MeetingInvite;
import org.propertymanagement.domain.TrackerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.propertymanagement.util.CorrelationIdUtil.correlationIdAsString;


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
            log.info("Successfully registered TrackerId={} CorrelationId={}", meetingInvite.getTrackerId().toString(), correlationIdAsString(meetingInvite.getCorrelationId()));
            return true;
        } catch (Exception e) {
            log.error("Error in registering trackerId={} Reason={} CorrelationId={}", meetingInvite.getTrackerId().toString(), e.getMessage(), correlationIdAsString(meetingInvite.getCorrelationId()));
            return false;
        }
    }

    @Override
    public MeetingInvite fetchMeetingInvite(TrackerId trackerId) {
        return repository.fetchMeetingInvite(trackerId);
    }
}
