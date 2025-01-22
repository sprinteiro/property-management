package org.propertymanagement.util;

import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import static org.hamcrest.MatcherAssert.assertThat;


public class CorrelationIdGeneratorTest {
    @Test
    void generateIdWithPrefix() {
        var idGenerator = new CorrelationIdGenerator();
        String id = idGenerator.nextRequestId();
        String hostnamePrefix = CorrelationIdGenerator.getHostnamePrefix();
        assertThat("No prefix", id.startsWith(hostnamePrefix));
        assertThat("No value", StringUtils.hasText(id.substring(hostnamePrefix.length())));
    }
}
