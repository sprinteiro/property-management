# Migration Playbook: Path to Spring Boot 4

## High-Level Strategies
- **Namespace:** Replace all `javax.*` with `jakarta.*` (Jakarta EE 11).
- **Jackson:** Use Jackson 3, replace all `com.fasterxml.jackson` with `tools.jackson`, except `jackson-annotations` module.
  Avoid Jackson 2 compatibility, refactor to Jackson 3, and use `JsonMapperBuilderCustomizer`.
- **Persistence**: New `spring-boot-persistence` module, adapt imports to use `org.springframework.boot.persistence.autoconfigure.EntityScan`
- **Testing:** Migrate `@MockBean` to `@MockitoBean`, `@SpyBean`to `@MockitoSpyBean`.
- **Web layer testing**: To provide MockMVC support for `@SpringBootTest`, add an `@AutoConfigureMockMvc` annotation to the test class.
  Also, consider replacing any use of `TestRestTemplate` with the new `RestTestClient` class. To configure this, add an `@AutoConfigureRestTestClient` annotation to the test class.
  (`org.springframework.boot:spring-boot-restclient`)
- **API & Client:** 
  - Prefer the new fluent `RestClient` over `RestTemplate`.
  - For reactive stacks, `WebClient` remains the choice.
  - Implement RFC 9457 `ProblemDetail` via a centralized `ResponseEntityExceptionHandler` for standard error reporting.
- **Resiliency:** Use Spring 7 native `@Retryable` for simple cases; Resilience4j for complex circuit breakers.
- **Concurrency & Virtual Threads:** 
  - Ensure `spring.threads.virtual.enabled=true` is set.
  - **Crucial:** Audit all codebase for `synchronized` blocks and replace with `ReentrantLock` to prevent Virtual Thread pinning (as per ADR-0002).
- **Modularization:** Don't use `spring-boot-starter-classic` and directly migrate to use the specific required modules.
  Ensure that only the specific modules for the logic's slide are provided in the specific `pom.xml`.
- **Spring Kafka Retry:** Spring Kafka has moved its retry capabilities from Spring Retry to Spring Framework.
  `spring.kafka.retry.topic.backoff.random` has been removed in favor of `spring.kafka.retry.topic.backoff.jitter`
- **Spring Security 7:** `@EnableMethodSecurity` is now the default; `PrePostEnabled` is true by default. Review security configurations to simplify and remove redundant annotations.
- **Spring testing features:**
  The `MockitoTestExecutionListener` is deprecated in Spring Boot 3.4. If `@Mock` or `@Captor` annotated fields aren’t working as expected, you should use `MockitoExtension` from Mockito itself.
- **Principles & Approval:** Always propose migration changes and ask for approval. Provide at least two configuration possibilities where applicable.
  Principles: simplicity, configuration reusability via `@Import`, library-based approach with POJOs, and avoiding component scanning (for lower memory footprint and faster startup).

## Reference documentation
- **API Versioning**: Suggest API versioning adoption or show ways to achieve versioning in a maintainable and non-invasive way.
  https://spring.io/blog/2025/09/16/api-versioning-in-spring
- **Spring Boot 4 migration guidelines**:
  https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide 
- **Upgrading to Jackson 3**:
  https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.0#major-changesfeatures-in-30 
- **Spring Kafka**:
  https://docs.spring.io/spring-kafka/reference/4.0/whats-new.html
  https://docs.spring.io/spring-boot/4.0-SNAPSHOT/appendix/application-properties/index.html#application-properties.integration.spring.kafka.retry.topic.backoff.jitter
- **Spring Security 7**:
  https://docs.spring.io/spring-security/reference/7.0/migration/
- **Spring 7**:
  https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-7.0-Release-Notes
