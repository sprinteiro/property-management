package org.propertymanagement.associationmeeting.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.propertymanagement.associationmeeting.exception.InvalidMeetingInviteException;
import org.propertymanagement.associationmeeting.exception.MeetingScheduleException;
import org.propertymanagement.associationmeeting.web.controller.NoOpController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@RestControllerAdvice(basePackageClasses = NoOpController.class)
public class ExceptionHandlerController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> defaultError(Exception exception, WebRequest request) {
        logException(exception, request);
        var error = new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", ""),
                "Unable to process the request"
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MeetingScheduleException.class)
    public ResponseEntity<ErrorMessage> meetingScheduleError(MeetingScheduleException exception, WebRequest request) {
        if (exception.getLogLevel() == MeetingScheduleException.LogLevel.WARN) {
            log.warn("Error in processing request. {} {}", exception.getMessage(), request.getContextPath());
        } else {
            log.error("Error in processing request. {} {}", exception.getMessage(), request.getContextPath(), exception);
        }
        var error = new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", ""),
                exception.getApiMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InvalidMeetingInviteException.class)
    public ResponseEntity<ErrorMessage> invalidMeetingInviteError(InvalidMeetingInviteException exception, WebRequest request) {
        logException(exception, request);
        var error = new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", ""),
                exception.getApiMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }

    private void logException(Exception exception, WebRequest request) {
        log.error("Error in processing request. {} {}", exception.getMessage(), request.getContextPath(), exception);
    }


    record ErrorMessage(int statusCode, String uri, String timestamp, String message) {
        public static final String DATE_TIME_FORMAT = "uuuu-MM-dd'T'HH:mm:s";

        public ErrorMessage(int statusCode, String uri, String message) {
            this(statusCode, uri, LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)), message);
        }
    }
}
