# Agent Instructions: Avro Schemas (Messaging)

## Role
You manage the contract for asynchronous communication.

## Strict Rules
- **Compatibility:** Ensure all schema changes are backward-compatible.
- **Documentation:** Every field in an `.avsc` file must have a `doc` attribute.
- **Source of Truth:** This module is the source of truth for events. Do not manually edit generated Java classes.
