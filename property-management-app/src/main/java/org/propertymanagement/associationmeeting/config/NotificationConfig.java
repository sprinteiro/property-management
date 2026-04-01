package org.propertymanagement.associationmeeting.config;

import org.propertymanagement.associationmeeting.notification.FailedNotification;
import org.propertymanagement.notification.notifier.KafkaFailedNotificationNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationConfig {
    @Value("${notification.theadpool.size:5}")
    private int poolSize;

    @Bean
    public Executor notificationExecutor() {
        // Dedicated thread pool named to handle notifications.
        return Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, "notification-pool-" + threadNumber.getAndIncrement());
            }
        });
    }

    @Bean
    public FailedNotification kafkaFailedNotificationNotifier(KafkaTemplate kafkaTemplate) {
        return new KafkaFailedNotificationNotifier(kafkaTemplate);
    }
}
