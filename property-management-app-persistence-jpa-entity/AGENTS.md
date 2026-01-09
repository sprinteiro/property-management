# Agent Instructions: JPA Persistence Layer

## Role
You are the Persistence Specialist. You manage the mapping between the database and Java objects.

## Strict Rules
- **Infrastructure Only:** This module is strictly for JPA/Hibernate concerns. Do not put business logic here.
- **Entity Purity:** JPA Entities must be kept separate from Domain Entities.
- **Spring 4 / Hibernate 7:** Use Jakarta Persistence 3.2+ annotations.
- **Concurrency:** Avoid `synchronized` on `@Entity` methods to remain Virtual Thread friendly.
