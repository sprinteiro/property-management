package org.propertymanagement.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Long.toHexString;
import static java.lang.System.currentTimeMillis;

/**
 * Request Id Generator
 */
public class CorrelationIdGenerator {
    /**
     * Header key for log request id values
     */
    public static final String MDC_CORRELATION_ID = "CorrelationId";
    public static final String SEPARATOR = "-";

    private final String hostPrefix = getHostName() + SEPARATOR;
    private final AtomicLong counter = new AtomicLong(currentTimeMillis() / 1000);


    /**
     * Generates the next id in the series
     *
     * @return next id
     */
    public String nextRequestId() {
        return hostPrefix + toHexString(counter.incrementAndGet());
    }

    public static String getHostnamePrefix() {
        return getHostName() + SEPARATOR;
    }

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName().split("\\.")[0];
        } catch (final UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

}
