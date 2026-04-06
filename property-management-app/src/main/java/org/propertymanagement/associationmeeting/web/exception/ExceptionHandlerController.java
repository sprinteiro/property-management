package org.propertymanagement.associationmeeting.web.exception;

import org.propertymanagement.associationmeeting.exception.InvalidMeetingInviteException;
import org.propertymanagement.associationmeeting.exception.MeetingScheduleException;
import org.propertymanagement.associationmeeting.web.controller.MeetingController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scoped Exception Handler for the Association Meeting module.
 * Adheres to RFC 9457 (Problem Details for HTTP APIs) as per ADR-0005.
 * Strictly scoped to MeetingController to ensure no side effects in other modules.
 */
@RestControllerAdvice(assignableTypes = MeetingController.class)
public class ExceptionHandlerController {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerController.class);
    private static final String DATE_TIME_FORMAT = "uuuu-MM-dd'T'HH:mm:ss";
    public static final String API_ERROR_UNABLE_TO_PROCESS_THE_REQUEST = "Unable to process the request";

    @ExceptionHandler(Exception.class)
    public ProblemDetail defaultError(Exception exception, WebRequest request) {
        logException(exception, request);
        return createProblemDetail(HttpStatus.BAD_REQUEST, API_ERROR_UNABLE_TO_PROCESS_THE_REQUEST, "Internal Error", request);
    }

    @ExceptionHandler(MeetingScheduleException.class)
    public ProblemDetail meetingScheduleError(MeetingScheduleException exception, WebRequest request) {
        if (exception.getLogLevel() == MeetingScheduleException.LogLevel.WARN) {
            log.warn("Error in processing [Association Meeting] request. {} {}", exception.getMessage(), request.getContextPath());
        } else {
            log.error("Error in processing [Association Meeting] request. {} {}", exception.getMessage(), request.getContextPath(), exception);
        }

        return createProblemDetail(HttpStatus.BAD_REQUEST, exception.getApiMessage(), "Meeting Scheduling Failed", request);
    }

    @ExceptionHandler(InvalidMeetingInviteException.class)
    public ProblemDetail invalidMeetingInviteError(InvalidMeetingInviteException exception, WebRequest request) {
        logException(exception, request);
        return createProblemDetail(HttpStatus.BAD_REQUEST, exception.getApiMessage(), "Invalid Meeting Invite", request);
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String detail, String title, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(path));
        // RFC 9457 Extension: Adding timestamp
        problem.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        return problem;
    }

    private void logException(Exception exception, WebRequest request) {
        log.error("Error in [Association Meeting] request. {} Path: {}", exception.getMessage(), request.getDescription(false), exception);
    }
}
