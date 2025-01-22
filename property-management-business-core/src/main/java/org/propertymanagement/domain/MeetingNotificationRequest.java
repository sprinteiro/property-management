package org.propertymanagement.domain;

public record MeetingNotificationRequest(
        Participant recipient,
        MeetingDate date,
        MeetingTime time,
        MeetingSubject meetingSubject,
        byte[] correlationId) {
}
