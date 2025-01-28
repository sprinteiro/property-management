package org.propertymanagement.associationmeeting.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@ConditionalOnProperty(name = "kafka.topic.creation", havingValue = "on")
@Slf4j
public class KafkaTopicsConfig {
    private static final int TOPIC_TOTAL_PARTITIONS = 3;
    private static final int TOPIC_TOTAL_REPLICAS = 3;
    public static final String TOPIC_NOTIFICATION_REQUEST_DLT = "notification-request-dlt";
    public static final String TOPIC_MEETING_REGISTRATION_REQUEST = "meeting-registration-request";
    public static final String TOPIC_MEETING_APPROVAL_REQUEST = "meeting-approval-request";
    public static final String TOPIC_NOTIFICATION_REQUEST = "notification-request";

    @Bean
    public NewTopic meetingRequest() {
        log.info("About to create topic " + TOPIC_MEETING_REGISTRATION_REQUEST);
        return TopicBuilder.name(TOPIC_MEETING_REGISTRATION_REQUEST)
                .partitions(TOPIC_TOTAL_PARTITIONS)
                .replicas(TOPIC_TOTAL_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic meetingApprovalRequest() {
        log.info("About to create topic " + TOPIC_MEETING_APPROVAL_REQUEST);
        return TopicBuilder.name(TOPIC_MEETING_APPROVAL_REQUEST)
                .partitions(TOPIC_TOTAL_PARTITIONS)
                .replicas(TOPIC_TOTAL_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic notificationRequest() {
        log.info("About to create topic " + TOPIC_NOTIFICATION_REQUEST);
        return TopicBuilder.name(TOPIC_NOTIFICATION_REQUEST)
                .partitions(TOPIC_TOTAL_PARTITIONS)
                .replicas(TOPIC_TOTAL_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic notificationRequestDlt() {
        log.info("About to create " + TOPIC_NOTIFICATION_REQUEST_DLT);
        return TopicBuilder.name(TOPIC_NOTIFICATION_REQUEST_DLT)
                .partitions(TOPIC_TOTAL_PARTITIONS)
                .replicas(TOPIC_TOTAL_REPLICAS)
                .build();
    }

}
