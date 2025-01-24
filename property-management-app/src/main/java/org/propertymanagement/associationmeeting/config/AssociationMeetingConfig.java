package org.propertymanagement.associationmeeting.config;

import org.propertymanagement.associationmeeting.MeetingScheduler;
import org.propertymanagement.associationmeeting.notification.FailedNotification;
import org.propertymanagement.associationmeeting.notification.MeetingNotification;
import org.propertymanagement.associationmeeting.notification.MeetingNotificationService;
import org.propertymanagement.associationmeeting.notifier.ManualMeetingNotifier;
import org.propertymanagement.associationmeeting.repository.MeetingRepository;
import org.propertymanagement.associationmeeting.repository.TrackerIdRepository;
import org.propertymanagement.neighbour.repository.NeighbourRepository;
import org.propertymanagement.notification.*;
import org.propertymanagement.util.CorrelationIdLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.Executor;

@Configuration
@Import(value = {
        NotificationConfig.class,
        RepositoriesConfig.class,
        WebConfig.class,
        WebSecurityConfig.class,
        KafkaConfig.class,
        LogConfig.class,
        EmailConfig.class,
        SmsConfig.class,
        WebNotificationMeetingConfig.class
})
public class AssociationMeetingConfig {
    @Bean
    public MeetingNotification manualMeetingNotifier(NotificationManager notificationManager, MeetingRepository meetingRepository, CorrelationIdLog correlationIdLog) {
        return new ManualMeetingNotifier(notificationManager, meetingRepository, correlationIdLog);
    }

    @Bean
    public MeetingNotification meetingNotificationService(@Value("${meeting.approval.automatic:true}") boolean automaticMeetingApproval,
                                                          MeetingNotification kafkaMeetingNotifier,
                                                          MeetingNotification manualMeetingNotifier
    ) {
        return new MeetingNotificationService(automaticMeetingApproval, kafkaMeetingNotifier, manualMeetingNotifier);
    }

    @Bean
    public NotificationManager notificationManager(
            EmailNotificationSender emailNotificationSender,
            SmsNotificationSender smsNotificationSender,
            Executor notificationExecutor,
            CorrelationIdLog correlationIdLog,
            FailedNotification kafkaFailedNotificationNotifier) {
        return new DefaultNotificationManager(emailNotificationSender, smsNotificationSender, notificationExecutor, correlationIdLog, kafkaFailedNotificationNotifier);
    }

    @Bean
    public MeetingScheduler meetingScheduler(MeetingRepository meeetingRepository,
                                             NeighbourRepository neighbourRepository,
                                             MeetingNotification meetingNotificationService,
                                             TrackerIdRepository trackerIdRepository,
                                             CorrelationIdLog correlationIdLog) {
        return new MeetingScheduler(
                meeetingRepository,
                neighbourRepository,
                meetingNotificationService,
                trackerIdRepository,
                correlationIdLog
        );
    }
}
