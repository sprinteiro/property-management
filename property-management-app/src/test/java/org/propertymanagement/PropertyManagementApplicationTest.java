package org.propertymanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("h2")
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "kafka.topic.creation=on",
        "debug=false"
}
)
@EmbeddedKafka
class PropertyManagementApplicationTest {
    @Test
    void contextLoads() {
    }
}
