package org.propertymanagement.domain.notification;

import org.propertymanagement.domain.*;

public record Meeting(
        MeetingDate date,
        MeetingTime time,
        MeetingSubject meetingSubject,
        byte[] correlationId) {
}
