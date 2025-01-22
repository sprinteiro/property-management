package org.propertymanagement.util;

import java.nio.charset.StandardCharsets;

public interface CorrelationIdUtil {

    static String correlationIdAsString(byte[] correlationId) {
        return new String(correlationId, StandardCharsets.UTF_8);
    }

    static byte[] correlationIdAsBytes(String correlationId) {
        return correlationId.getBytes(StandardCharsets.UTF_8);
    }
}
