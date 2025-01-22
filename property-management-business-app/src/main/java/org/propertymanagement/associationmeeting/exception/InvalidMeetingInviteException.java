package org.propertymanagement.associationmeeting.exception;

public class InvalidMeetingInviteException extends IllegalArgumentException {
    private final String apiMessage;


    public InvalidMeetingInviteException(String detailedMessage, String apiMessage) {
        super(detailedMessage);
        this.apiMessage = apiMessage;
    }

    public String getApiMessage() {
        return this.apiMessage;
    }
}
