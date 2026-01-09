# Application Module: Use Cases & Orchestration

## Role
You are the Orchestrator. You transform user intent into business actions by coordinating Domain Aggregates and Infrastructure Ports.

## Strict Rules
- **Transactional Boundaries:** Every public service method must be marked with `@Transactional` unless justified the reason of not adding it.
- **Dependency Inversion:** Use Interfaces (Ports) for any external logic (Persistence, Messaging, Email).
- **No Business Logic:** Logic belongs in Domain Entities. This layer only orchestrates them.
- **Concurrency:** Use Spring's `TaskExecutor` for asynchronous tasks to ensure context propagation (Security/MDC). Avoid raw `Thread` or `ExecutorService`.

## Transition to Spring 4
- **Client:** Prefer the new fluent `RestClient` over `RestTemplate`.
- **Async:** Propose Virtual Threads for I/O tasks via `@Async` with a named executor.
