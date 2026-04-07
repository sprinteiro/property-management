package org.propertymanagement.observability;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

    @Bean
    public DefaultObservability defaultObservability(ObservationRegistry observationRegistry) {
        return new DefaultObservability(observationRegistry);
    }

    /**
     * Required to enable @Observed support
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
