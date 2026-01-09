/**
 * <h2>Architectural Boundary Enforcement</h2>
 *
 * <p>This package contains ArchUnit tests designed to programmatically enforce
 * the <b>Clean Architecture</b> boundaries defined in the root AGENTS.md.</p>
 *
 * <h3>Motivation:</h3>
 * <ul>
 * <li><b>Common-Closure Principle (CCP):</b> These rules are grouped together because
 * they change only when the overall system architecture evolves.</li>
 * <li><b>Dependency Law:</b> To ensure the Business Core remains "Pure" and
 * uncontaminated by Infrastructure (JPA/Spring) or Application concerns.</li>
 * <li><b>Maintainability:</b> Automated tests prevent "Architectural Erosion"
 * over time as the team or AI agents grow the codebase.</li>
 * </ul>
 */
package com.property.management.architecture;
