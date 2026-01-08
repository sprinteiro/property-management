## 🎯 Purpose & Value
## 🛠 Architectural Compliance
- [ ] Clean Architecture: No domain leakage.
- [ ] Java 25: Virtual thread safe (no `synchronized` blocks).
- [ ] ADR Compliance: Follows ADR-0002 (Concurrency) and ADR-0004 (Observation).

## 🛡 Resiliency & Testing
- [ ] Every external call has a Retry/Circuit Breaker.
- [ ] F.I.R.S.T. Unit tests cover all logic paths.
- [ ] Integration tests use Testcontainers.
