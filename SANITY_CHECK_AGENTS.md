# Mission Directive: Pre-Migration Sanity Check

## 🎯 Primary Objective
Audit the current `property-management-business-core` and `business-app` modules to ensure zero framework contamination and perfect layer separation before the Java 25 migration.

## 🛠 Active Audit Steps (The Sanity Check)
1. **Framework Scan:** - Flag any `import` in `business-core` that starts with `org.springframework` or `jakarta.persistence`.
    - Goal: Business logic must be 100% POJO (Plain Old Java Objects).
2. **Lombok Extraction Analysis:**
    - Identify `@Data` or `@Entity` annotations in the Core.
    - Propose a plan to replace them with **Immutable Java Records** (to prepare for Java 25).
3. **Boundary Verification:**
    - Check if `business-app` is accidentally using JPA Entities.
    - Ensure `business-app` defines **Interface Ports** and doesn't know about `persistence-jpa-entity` implementations.
4. **Exception Handling Audit:**
    - Ensure the Domain Core throws **Domain Exceptions**, not `SQLException` or `PersistenceException`.

## 🤝 Pairing Protocol
- For every file audited, the Agent must report: "CLEAN", "NEEDS REFACTOR", or "VIOLATION".
- If a violation is found, propose a **minimalist refactor** that respects the current tech stack (Pre-Migration).