# ADR 0004: Observation & Monitoring via Spring 7

## Status
Accepted

## Context
Modern systems need unified observability (Metrics + Tracing) to diagnose failures in distributed or modular environments.

## Decision
Adopt the **Spring 7 Observation API** as the primary instrumentation tool.
- Use `@Observed` for automatic service timing.
- Use `ObservationRegistry` in Infrastructure adapters for external calls.

## Rationale
- **Unified Logic:** One instrumentation point creates both a Prometheus metric and an OTLP trace.
- **Java 25 Ready:** Designed to work natively with Virtual Threads and Scoped Values.

## Consequences
- **Positive:** Consistent visibility across all layers.
- **Negative:** Requires migration away from legacy Micrometer-specific annotations.
