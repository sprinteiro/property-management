# Agent Instructions: Business Application (Use Cases)

## Role
You are the Orchestrator. You coordinate Domain Aggregates to fulfill user requests.

## Strict Rules
- **Transactional:** All public methods must be `@Transactional`.
- **Inversion:** Depend on interfaces (Ports) defined here but implemented in Infrastructure.
- **Context:** Use Spring `TaskExecutor` for any async work to ensure Security/Tracing context propagation (ADR-0002).
