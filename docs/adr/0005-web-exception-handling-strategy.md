# ADR-0005: Web Exception Handling Strategy (RFC 9457)

## Status
Proposed

## Context
As we migrate to Spring Boot 4 and Spring Framework 7, we need a standardized way to communicate error details to API clients. Historically, the project used custom `ErrorMessage` records. Spring 7 now provides native support for **RFC 9457 (Problem Details for HTTP APIs)** via the `ProblemDetail` class.

Additionally, to maintain the integrity of our Clean Architecture and the **Common-Closure Principle (CCP)**, we must ensure that cross-cutting concerns like exception handling do not leak across module boundaries.

## Decision
1.  **Standardization:** Use `org.springframework.http.ProblemDetail` as the standard return type for all REST API error responses.
2.  **Scoping (Isolation):** All `@RestControllerAdvice` implementations MUST be explicitly scoped to prevent side effects in other modules. Use attributes like `assignableTypes`, `basePackageClasses`, or `annotations` to restrict the advice to specific controllers or modules.
3.  **Modularization:** Each module/use-case should have its own scoped exception handler to encapsulate its domain-specific error mapping logic.
4.  **RFC Compliance:** Error responses should include standard fields: `type`, `title`, `status`, `detail`, and `instance` (the URI path).

## Consequences
- **Positive:** Standardized error responses improve interoperability with external clients and infrastructure (gateways, meshes).
- **Positive:** Improved module isolation by preventing one module's exception handling logic from affecting another.
- **Negative:** Slightly more boilerplate as each module needs its own handler, though common handlers can still be used for generic infrastructure errors (e.g., Auth, 500s) if explicitly shared.

## Alternatives Considered
- **Global `ExceptionHandler`:** Rejected because it violates the principle of module isolation and CCP.
- **Custom Error DTOs:** Rejected in favor of the industry-standard RFC 9457.
