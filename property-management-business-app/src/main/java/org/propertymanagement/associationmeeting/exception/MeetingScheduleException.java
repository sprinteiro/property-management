package org.propertymanagement.associationmeeting.exception;

public class MeetingScheduleException extends RuntimeException {
    private final String apiMessage;
    private final LogLevel logLevel;

    public MeetingScheduleException(String message, String apiMessage, LogLevel logLevel) {
        super(message);
        this.apiMessage = apiMessage;
        this.logLevel = logLevel;
    }

    public MeetingScheduleException(String message, String apiMessage) {
        this(message, apiMessage, LogLevel.ERROR);
    }

    public MeetingScheduleException(String errorMessage, LogLevel logLevel) {
        this(errorMessage, null, logLevel);
    }

    public MeetingScheduleException(String message) {
        this(message, message);
    }

    public enum LogLevel {
        ERROR,
        WARN
    }

    public String getApiMessage() {
        return apiMessage;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }
}
