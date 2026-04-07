package org.propertymanagement.associationmeeting.config;

import org.apache.avro.generic.GenericRecord;
import org.propertymanagement.associationmeeting.notification.FailedNotification;
import org.propertymanagement.notification.notifier.KafkaFailedNotificationNotifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class NotificationConfig {
    @Value("${notification.theadpool.size:5}")
    private int poolSize;

    @Bean
    public Executor notificationExecutor() {
        // Dedicated thread pool named to handle notifications using Spring's TaskExecutor for context propagation.
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("notification-pool-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean
    public FailedNotification kafkaFailedNotificationNotifier(KafkaTemplate<String, GenericRecord> kafkaTemplate) {
        return new KafkaFailedNotificationNotifier(kafkaTemplate);
    }
}
