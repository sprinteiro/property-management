# Migration Playbook: Path to Spring Boot 4

## High-Level Strategies
- **Namespace:** Replace all `javax.*` with `jakarta.*` (Jakarta EE 11).
- **Testing:** Migrate `@MockBean` to `@MockitoBean`.
- **API:** Prefer the new fluent `RestClient` over `RestTemplate`.
- **Resiliency:** Use Spring 7 native `@Retryable` for simple cases; Resilience4j for complex circuit breakers.
