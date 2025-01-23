package org.propertymanagement.associationmeeting.config;

import org.propertymanagement.associationmeeting.MeetingScheduler;
import org.propertymanagement.associationmeeting.web.controller.MeetingController;
import org.propertymanagement.associationmeeting.web.exception.ExceptionHandlerController;
import org.springframework.context.annotation.Bean;

public class WebConfig {
    @Bean
    public MeetingController meetingController(MeetingScheduler meetingScheduler) {
        return new MeetingController(meetingScheduler);
    }

    @Bean
    public ExceptionHandlerController exceptionHandlerController() {
        return new ExceptionHandlerController();
    }
}
