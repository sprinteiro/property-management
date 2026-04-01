package org.propertymanagement.associationmeeting.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.propertymanagement.associationmeeting.MeetingScheduler;
import org.propertymanagement.associationmeeting.listener.KafkaMeetingApprovalListener;
import org.propertymanagement.associationmeeting.listener.KafkaMeetingCreationListener;
import org.propertymanagement.associationmeeting.listener.KafkaNotificationListener;
import org.propertymanagement.associationmeeting.notification.MeetingNotification;
import org.propertymanagement.associationmeeting.notifier.KafkaMeetingNotifier;
import org.propertymanagement.associationmeeting.repository.MeetingRepository;
import org.propertymanagement.notification.NotificationManager;
import org.propertymanagement.util.CorrelationIdLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;


@Configuration
@Import(value = {KafkaTopicsConfig.class})
public class KafkaConfig {
    @Value("${kafka.threadpool.size:5}")
    private int poolSize;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    /**
     * Configures the ProducerFactory using GenericRecord to satisfy Avro requirements.
     */
    @Bean
    public ProducerFactory<String, GenericRecord> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        // Mandatory for KafkaAvroSerializer to communicate with the registry
        configProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, GenericRecord> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public Executor kafkaExecutor() {
        // Dedicated thread pool named kafkaExecutor for handling asynchronous Kafka operations (Spring context awareness).
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("kafka-pool-");
        // Essential for preserving MDC and Security context across boundaries
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
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
    public MeetingNotification kafkaMeetingNotifier(KafkaTemplate<String, GenericRecord> kafkaTemplate, Executor kafkaExecutor, CorrelationIdLog correlationIdLog) {
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
