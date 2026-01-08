#!/bin/bash

# setup_existing_modules.sh
# Run this from the project root where your pom.xml is located.

echo "⚙️  Integrating Agents into existing modules..."

# 1. Place module-specific instructions
touch property-management-domain/AGENTS.md
touch property-management-app/AGENTS.md
touch property-management-infrastructure/AGENTS.md

# 2. Global files (these stay at the root)
touch AGENTS.md
touch TESTING_AGENTS.md
touch MIGRATION_CONTEXT.md

# 3. Documentation and GitHub files
mkdir -p docs/adr
mkdir -p .github
touch .github/PULL_REQUEST_TEMPLATE.md
touch docs/adr/0001-template.md
touch docs/adr/0002-concurrency-strategy.md
touch docs/adr/0004-observation-and-monitoring.md

echo "✅ Files created in existing modules. Ready for content paste!"#!/bin/bash

# setup_agents.sh
# Purpose: Initialize the AGENTS.md ecosystem for Property Management Project

echo "🚀 Initializing Agents.md ecosystem..."

# 1. Create Directory Structure
echo "📁 Creating directories..."
mkdir -p .github
mkdir -p docs/adr
mkdir -p domain
mkdir -p application
mkdir -p infrastructure

# 2. Create Global Context Files
echo "📝 Creating global context files..."
touch AGENTS.md
touch TESTING_AGENTS.md
touch MIGRATION_CONTEXT.md

# 3. Create Module-Specific Agent Files
echo "📝 Creating module agent files..."
touch domain/AGENTS.md
touch application/AGENTS.md
touch infrastructure/AGENTS.md

# 4. Create ADRs and PR Template
echo "📝 Creating ADRs and PR template..."
touch .github/PULL_REQUEST_TEMPLATE.md
touch docs/adr/0001-template.md
touch docs/adr/0002-concurrency-strategy.md
touch docs/adr/0004-observation-and-monitoring.md

echo "✅ Structure created successfully!"
echo "Next step: Paste the content provided in the 'Master Implementation Guide' into these files."