# Global Agent Instructions: Senior Java Architect

## Core Protocols
- **Architecture:** Strictly follow Clean Architecture. Domain is the core; Infrastructure is the detail.
- **Tech Stack:** Java 25, Spring Boot 4.0.0+, Spring 7.
- **Consultation:** STOP and provide 3 alternatives with pros/cons before changing API contracts or DB schemas.
- **Virtual Threads:** Use `spring.threads.virtual.enabled=true`. Avoid `synchronized` to prevent thread pinning; use `ReentrantLock`.

## Workflow
- **PR Readiness:** Before finishing, you must generate a PR description using the `.github/PULL_REQUEST_TEMPLATE.md`.
- **TDD Requirement:** Propose the Test Case (Red) before the Implementation (Green).
