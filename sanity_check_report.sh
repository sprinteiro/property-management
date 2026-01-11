#!/bin/bash

echo "🔍 [ARCHITECTURAL AUDIT] - Scanning for Pre-Migration Debt..."
echo "=========================================================="

# 1. Framework Contamination (Violation of Section 3)
echo -e "\n📍 [CHECK 1] Spring/JPA leakage in Business Core..."
grep -rE "import org.springframework|import jakarta.persistence|import jakarta.transaction" property-management-business-core/src/main/java | tee core_violations.log

# 2. Annotation Audit (Violation of Mission Objective 2)
echo -e "\n📍 [CHECK 2] Lombok/JPA Annotations in Domain Entities..."
grep -rE "@Data|@Entity|@Table|@Column|@Id" property-management-business-core/src/main/java | tee annotation_violations.log

# 3. Layer Boundary Audit (Violation of Dependency Law)
echo -e "\n📍 [CHECK 3] Business App importing Persistence Details..."
grep -r "import .*persistence.jpa.entity" property-management-business-app/src/main/java | tee boundary_violations.log

echo -e "\n=========================================================="
echo "✅ Audit Complete. Results saved to .log files."
echo "👉 Instructions: Feed these .log files to your AI agent to begin refactoring."