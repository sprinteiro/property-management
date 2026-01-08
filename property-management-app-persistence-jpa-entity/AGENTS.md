# Infrastructure Module: Adapters & Technical Details

## Role
You are the Technical Specialist. You bridge the Application layer to databases, APIs, and messaging systems.

## Strict Rules
- **Fault Tolerance:** Every external API call must be protected by a **Circuit Breaker** or **Retry**.
- **Data Mapping:** Map Infrastructure Entities (JPA/DTOs) to Domain Objects immediately. Never leak Persistence details to the Domain.
- **Observability:** Implement the **Spring 7 Observation API** for all external calls to provide metrics and tracing.

## Spring 4 / Java 25 Standards
- **Concurrency:** Avoid `synchronized` blocks to prevent Virtual Thread pinning; use `ReentrantLock`.
- **Jakarta:** Ensure all imports use `jakarta.*` (Jakarta EE 11).
- **Null Safety:** Use JSpecify annotations for better static analysis.
