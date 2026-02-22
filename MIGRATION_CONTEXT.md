# Migration Playbook: Path to Spring Boot 4

## High-Level Strategies
- **Namespace:** Replace all `javax.*` with `jakarta.*` (Jakarta EE 11).
- **Jackson:** Use Jackson 3, replace all `com.fasterxml.jackson`  with `tools.jackson`, except `jackson-annotations` module.
  Avoid Jackson 2 compatibility and refactor to Jackson 3, and use `JsonMapperBuilderCustomizer`.
- **Persistence**: New `spring-boot-persistence` module, adapt imports to use `org.springframework.boot.persistence.autoconfigure.EntityScan`
  - **Testing:** Migrate `@MockBean` to `@MockitoBean`, `@SpyBean`to `@MockitoSpyBean`.
- **Web layer testing**: To provide MockMVC support for `@SpringBootTest`, add an `@AutoConfigureMockMvc annotation to the test class.
  Also, consider replacing any use of `TestRestTemplate` with the new `RestTestClient` class. To configure this, add an `@AutoConfigureRestTestClient` annotation to the test class.
- **API:** Prefer the new fluent `RestClient` over `RestTemplate`.
- **Resiliency:** Use Spring 7 native `@Retryable` for simple cases; Resilience4j for complex circuit breakers.
- Don't use `spring-boot-starter-classic` and directly migrate to use the specific required modules by following the new modularization approach.
  Ensure that only the specific modules for the logic's slide are provided in the specific `pom.xml`.
- Always propose the migration changes and ask for approval, and ideally provide at least two configuration posibilities.
  Princpiples: simplicity, configuration reusability via `@Import` always with mindset of library based in POJOs and avoiding component scanning
  (to avoid potential classpath classing, reduced in-memory footprint and fast start-up time).

## Reference documentation
- **API Versioning**: Suggest API versioning adoption or show ways to achieve versioning in a maintainable and non-invasive way.
  https://spring.io/blog/2025/09/16/api-versioning-in-spring
- **Spring Boot 4 migration guidelines**:
  https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide 
