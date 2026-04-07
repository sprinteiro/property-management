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
import org.propertymanagement.observability.ObservabilityConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.concurrent.Executor;

@Import(value = {
        JpaAssociationMeetingRepositoriesConfig.class,
        WebConfig.class,
        WebSecurityConfig.class,
        KafkaConfig.class,
        LogConfig.class,
        EmailConfig.class,
        SmsConfig.class,
        WebNotificationMeetingConfig.class,
        ObservabilityConfig.class,
        NotificationConfig.class
})
public class AssociationMeetingConfig {
    @Bean
    public MeetingNotification manualMeetingNotifier(NotificationManager notificationManager, MeetingRepository meetingRepository) {
        return new ManualMeetingNotifier(notificationManager, meetingRepository);
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
            FailedNotification kafkaFailedNotificationNotifier) {
        return new DefaultNotificationManager(emailNotificationSender, smsNotificationSender, notificationExecutor, kafkaFailedNotificationNotifier);
    }

    @Bean
    public MeetingScheduler meetingScheduler(MeetingRepository meeetingRepository,
                                             NeighbourRepository neighbourRepository,
                                             MeetingNotification meetingNotificationService,
                                             TrackerIdRepository trackerIdRepository) {
        return new MeetingScheduler(
                meeetingRepository,
                neighbourRepository,
                meetingNotificationService,
                trackerIdRepository
        );
    }
}
