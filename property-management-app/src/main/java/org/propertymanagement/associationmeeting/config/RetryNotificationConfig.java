package org.propertymanagement.associationmeeting.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.propertymanagement.notification.exception.NotificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Predicate;

import static java.time.temporal.ChronoUnit.MILLIS;

@ConditionalOnProperty(name = "notification.retry", havingValue = "on")
@Configuration
@Slf4j
public class RetryNotificationConfig {
    @Bean
    public Retry retryNotification(
            @Value("${notification.retry.number:3}") int maxAttempts,
            @Value("${notification.retry.waitduration:1000}") long waitDuration) {
        log.info("Creating Retry Resilience4j");
        Predicate<Throwable> onlyWhenRetriableNotification =
                throwable ->
                        throwable instanceof NotificationException;

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(Duration.of(waitDuration, MILLIS))
                .retryOnException(onlyWhenRetriableNotification)
                .build();
        RetryRegistry registry = RetryRegistry.of(config);
        Retry retryNotification = registry.retry("retryNotification", config);
        Retry.EventPublisher publisher = retryNotification.getEventPublisher();
        publisher.onRetry(event ->      log.info("--------------------- onRetry -->{}", event));
        publisher.onSuccess(event ->  log.info("---------------------  onSuccess -->{}", event));
        return retryNotification;
    }
}
