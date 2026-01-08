# ADR 0002: Concurrency Strategy (Java 25 & Spring Boot 4)

## Status
Accepted

## Context
Traditional platform threads are expensive. Java 25 Virtual Threads solve this but require careful handling of thread-local contexts and avoidance of thread pinning.

## Decision
We will use **Virtual Threads** via Spring's `TaskExecutor` abstraction.
- Enable `spring.threads.virtual.enabled=true`.
- Use `ThreadPoolTaskExecutor` beans for managed context propagation.

## Rationale
- **Throughput:** Virtual threads allow nearly unlimited blocking I/O tasks.
- **Context:** Spring's executor ensures `SecurityContext` and `MDC` logs are transferred to the new thread.
- **Production Tuning:** Allows pool sizes to be adjusted via `application.yml`.

## Consequences
- **Positive:** High scalability for Property Management API calls.
- **Negative:** Must audit code for `synchronized` blocks which cause pinning.
