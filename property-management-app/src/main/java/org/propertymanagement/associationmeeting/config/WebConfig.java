package org.propertymanagement.associationmeeting.config;

import org.propertymanagement.associationmeeting.MeetingScheduler;
import org.propertymanagement.associationmeeting.web.controller.MeetingController;
import org.springframework.context.annotation.Bean;

public class WebConfig {
    @Bean
    public MeetingController meetingController(MeetingScheduler meetingScheduler) {
        return new MeetingController(meetingScheduler);
    }
}
