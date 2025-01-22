package org.propertymanagement.notification.exception;

public class NotificationException extends RuntimeException {
    private final Long participantId;
    private final String apiMessage;
    private final Exception exception;

    public NotificationException(Long participantId, String message, String apiMessage, Exception exception) {
        super(message);
        this.participantId = participantId;
        this.apiMessage = apiMessage;
        this.exception = exception;
    }

    public NotificationException(String message, Exception e) {
        this(null ,message, null, e);
    }

    public NotificationException(String message) {
        this(null, message, null, null);
    }

    public NotificationException(Long participantId, String message, Exception exception) {
        this(participantId, message, null, exception);
    }


    @Override
    public String toString() {
        return String.format("Message: %s", this.getMessage());
    }

    public String getApiMessage() {
        return apiMessage;
    }

}
