package org.propertymanagement.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

import java.util.function.Supplier;

public class DefaultObservability {
    private final ObservationRegistry registry;

    public DefaultObservability(ObservationRegistry registry) {
        this.registry = registry;
    }

    public <T> T observe(String name, String contextualName, Supplier<T> supplier) {
        return Observation.createNotStarted(name, registry)
                .contextualName(contextualName)
                .observe(supplier);
    }

    public void observe(String name, String contextualName, Runnable runnable) {
        Observation.createNotStarted(name, registry)
                .contextualName(contextualName)
                .observe(runnable);
    }
}
