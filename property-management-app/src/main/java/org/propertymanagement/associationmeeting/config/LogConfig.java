package org.propertymanagement.associationmeeting.config;

import org.propertymanagement.util.CorrelationIdLog;
import org.propertymanagement.util.CorrelationIdLogUtils;
import org.springframework.context.annotation.Bean;

public class LogConfig {
    @Bean
    public CorrelationIdLog correlationIdLog() {
        return new CorrelationIdLogUtils();
    }
}
