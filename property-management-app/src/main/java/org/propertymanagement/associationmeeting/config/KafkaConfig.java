package org.propertymanagement.associationmeeting.config;

import lombok.extern.slf4j.Slf4j;
import org.propertymanagement.associationmeeting.MeetingScheduler;
import org.propertymanagement.associationmeeting.listener.KafkaMeetingApprovalListener;
import org.propertymanagement.associationmeeting.listener.KafkaMeetingCreationListener;
import org.propertymanagement.associationmeeting.listener.KafkaNotificationListener;
import org.propertymanagement.associationmeeting.notification.MeetingNotification;
import org.propertymanagement.associationmeeting.notifier.KafkaMeetingNotifier;
import org.propertymanagement.associationmeeting.repository.MeetingRepository;
import org.propertymanagement.notification.NotificationManager;
import org.propertymanagement.util.CorrelationIdLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


@Import(value = {KafkaTopicsConfig.class})
@Slf4j
public class KafkaConfig {

    @Value("${kafka.theadpool.size:5}")
    private int poolSize;

    @Bean
    public Executor kafkaExecutor() {
        // Dedicated thread pool named kafkaExecutor for handling asynchronous Kafka operations.
        return Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
            private AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, "kafka-pool-" + threadNumber.getAndIncrement());
            }
        });
    }

    @Bean
    public KafkaMeetingCreationListener kafkaMeetingCreationListener(MeetingRepository meetingRepository, MeetingScheduler meetingScheduler, CorrelationIdLog correlationIdLog) {
        return new KafkaMeetingCreationListener(meetingRepository, meetingScheduler, correlationIdLog);
    }

    @Bean
    public KafkaMeetingApprovalListener kafkaMeetingApprovalListener(MeetingRepository meetingRepository, MeetingScheduler meetingScheduler, CorrelationIdLog correlationIdLog) {
        return new KafkaMeetingApprovalListener(meetingRepository, meetingScheduler, correlationIdLog);
    }

    @Bean
    public KafkaNotificationListener kafkaNotificationListener(NotificationManager notificationManager, CorrelationIdLog correlationIdLog) {
        return new KafkaNotificationListener(notificationManager, correlationIdLog);
    }

    @Bean
    public MeetingNotification kafkaMeetingNotifier(KafkaTemplate kafkaTemplate, Executor kafkaExecutor, CorrelationIdLog correlationIdLog) {
        return new KafkaMeetingNotifier(kafkaTemplate, kafkaExecutor, correlationIdLog);
    }

//    @Bean
//    public DefaultErrorHandler errorHandler(ConsumerRecordRecoverer recoverer) {
//        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer);
//        handler.addNotRetryableExceptions(FailedNotificationException.class);
//        return handler;
//    }

//    @Bean
//    public ConsumerRecordRecoverer consumerRecordRecoverer(KafkaTemplate kafkaTemplate, CorrelationIdLog correlationIdLog) {
//        return new KafkaNotificationRecoverer(kafkaTemplate, correlationIdLog);
//    }

}
