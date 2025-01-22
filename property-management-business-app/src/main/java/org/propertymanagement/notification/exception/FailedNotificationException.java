package org.propertymanagement.notification.exception;

public class FailedNotificationException extends RuntimeException {
    private final Exception exception;
    private final String apiMessage;
    private String reason;
    private Long recipientId;

    public FailedNotificationException(String message, String apiMessage, String reason, Long recipientId, Exception e) {
        super(message);
        this.apiMessage = apiMessage;
        this.reason = reason;
        this.recipientId = recipientId;
        this.exception = e;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getRecipientId() {
        return recipientId;
    }
}
