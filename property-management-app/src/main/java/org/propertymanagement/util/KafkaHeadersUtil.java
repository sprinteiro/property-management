package org.propertymanagement.util;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.support.KafkaHeaders;

import java.nio.charset.StandardCharsets;

import static java.util.Objects.nonNull;

public interface KafkaHeadersUtil {
    static String correlationIdAsString(ProducerRecord<String, GenericRecord> record) {
        Header correlationIdHeader = record.headers().lastHeader(KafkaHeaders.CORRELATION_ID);
        return nonNull(correlationIdHeader) ? new String(correlationIdHeader.value(), StandardCharsets.UTF_8) : null;
    }
    static String correlationIdAsString(byte[] correlationId) {
        return CorrelationIdUtil.correlationIdAsString(correlationId);
    }
}
