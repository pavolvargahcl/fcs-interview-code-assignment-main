# Fulfilment Warehouse — Architecture Decision Records
- This document captures key architectural decisions for the warehouse code assignment.
- Each decision balances clean architecture with the pragmatic constraints of a single-module assessment project.

---

## ADR: Feature-Oriented Package Structure
Decision: Organise packages by business feature, not by technical layer.
Context: The original codebase mixed flat feature packages (products, stores) with a deeply-nested hexagonal layout for warehouses (`adapters/database/`, `adapters/restapi/`, `domain/models/`, `domain/ports/`, `domain/usecases/`). This forces developers to navigate technical layers rather than business capabilities.

Rationale:
- "I'm working on warehouse creation" → navigate to `warehouses/create/`
- "I'm working on archival" → navigate to `warehouses/archive/`
- Feature root holds shared types (model, ports, persistence, REST adapter) — the cohesive "warehouse" concept
- Sub-packages are named by business operation, not by technical role

| Before (technical layers)       | After (feature-oriented)                       |
|---------------------------------|------------------------------------------------|
| `warehouses/domain/models/`     | `warehouses/` (feature root)                   |
| `warehouses/domain/ports/`      | `warehouses/` + `warehouses/create/` etc.      |
| `warehouses/domain/usecases/`   | `warehouses/create/`, `/archive/`, `/replace/` |
| `warehouses/adapters/database/` | `warehouses/` (feature root)                   |
| `warehouses/adapters/restapi/`  | `warehouses/` (feature root)                   |

Trade-off: Shared types (Warehouse, WarehouseStore) sit alongside infrastructure (DbWarehouse, WarehouseRepository) in the feature root. Acceptable at this project scale; a larger codebase would introduce `model/` and `persistence/` sub-packages.

Reference: `ADR-Feature-Oriented-Project.md`

---

## ADR: Hexagonal Architecture Within the Warehouse Feature
Decision: Use ports-and-adapters (hexagonal) architecture for warehouse business logic, with use cases implementing port interfaces.
Context: The assignment required multiple CRUD operations with non-trivial validation rules (location capacity, BU code uniqueness, stock limits). Pure procedural code in a REST resource would become untestable and rigid.

Rationale:

| Component        | Role                              | Example                                                     |
|------------------|-----------------------------------|-------------------------------------------------------------|
| Port (interface) | Defines what the domain needs     | `WarehouseStore`, `CreateWarehouseOperation`                |
| Use case         | Implements business rules         | `CreateWarehouseUseCase` (5 validations)                    |
| Adapter          | Connects domain to infrastructure | `WarehouseRepository` (JPA), `WarehouseResourceImpl` (REST) |

- Ports allow unit-testing use cases with Mockito stubs — no container needed, millisecond execution
- The REST adapter (`WarehouseResourceImpl`) only maps DTOs and delegates to ports
- Persistence adapter (`WarehouseRepository`) implements `WarehouseStore` via Panache

Trade-off: More files than a flat approach; justified by testability and the assignment's explicit hexagonal requirement.

---

## ADR: CDI Events for Post-Commit Legacy System Sync
Decision: Use CDI `Event<StoreChangeEvent>` with `@Observes(during = TransactionPhase.AFTER_SUCCESS)` for legacy gateway calls.
Context: The `StoreResource` must call `LegacyStoreManagerGateway` after creating/updating stores. Direct calls inside the transaction risk notifying the legacy system before the DB commit succeeds (or rolling back without notifying).

Rationale:
- `TransactionPhase.AFTER_SUCCESS` guarantees the observer fires only after a successful commit
- Decouples the resource from the gateway — SRP: resource handles HTTP, observer handles side-effects
- If the transaction rolls back, the legacy system is never notified (correct behavior)

Implementation: `StoreResource` fires `StoreChangeEvent`; `LegacyStoreChangeObserver` receives it and delegates to the gateway.

Trade-off: Slightly more indirection; justified by transactional correctness.

---

## ADR: OpenAPI-First for Warehouse API
Decision: Generate the `WarehouseResource` JAX-RS interface from `warehouse-openapi.yaml` using `quarkus-openapi-generator-server`; implement it in `WarehouseResourceImpl`.
Context: The assignment provided an OpenAPI spec. Code-first and spec-first are both valid; we chose spec-first because the spec was given.

Rationale:
- Contract is the single source of truth — no drift between spec and implementation
- Generated interface enforces path, method, parameter, and response types at compile time
- `WarehouseResourceImpl` only contains business delegation logic, no JAX-RS annotation boilerplate

Trade-off: Generated code in `target/` requires a build step before IDE navigation works. Acceptable for a Quarkus project where `quarkus:dev` handles this automatically.

---

## ADR: Explicit Types Over `var`
Decision: Use explicit type declarations (e.g. `Warehouse existing = ...`) instead of `var` throughout the codebase.
Context: Java 10+ `var` reduces verbosity but removes type information from the source line.

Rationale:
- AI agent processing: Explicit types provide stronger reinforcement for code understanding, generation and review by AI coding agents (Claude, Copilot, etc.) — the type context on every line eliminates ambiguity
- Human reviewers without IDE: In code reviews (GitHub PR diff, terminal, printed code), there is no hover-for-type — explicit types make the code self-documenting
- Test readability: In Given/When/Then test sections, seeing `Warehouse existing = buildWarehouse(...)` immediately communicates the domain concept

Trade-off: Slightly more verbose; justified by the readability and AI-processing benefits in an assessment context.

---

## ADR: Test Pyramid with Logical Separation
Decision: Three test levels — unit, integration, E2E — logically separated within a single Maven module.
Context: The test pyramid recommends many fast unit tests, fewer integration tests, and minimal E2E tests. Ideally each level lives in a separate module for structural enforcement.

Rationale:

| Level       | Naming              | Runner                    | Speed        | What it tests               |
|-------------|---------------------|---------------------------|--------------|-----------------------------|
| Unit        | `*Test.java`        | Surefire + Mockito        | Milliseconds | Business logic in isolation |
| Integration | `*IT.java`          | Surefire + `@QuarkusTest` | Seconds      | HTTP → CDI → JPA stack      |
| E2E         | `*.http` (IntelliJ) | Manual / CI plugin        | Seconds      | Full running instance       |

- Unit tests use Mockito stubs — no container, no database
- Integration tests boot Quarkus with H2 in-memory database (`@QuarkusTest`)
- E2E tests are IntelliJ HTTP Request files (`src/test/resources/e2e/`) with JavaScript assertions

Production module split (not possible in this single-module assessment):
```
warehouse-domain/src/test/          → unit tests only
warehouse-integration-tests/        → @QuarkusTest, test-containers
warehouse-e2e-tests/                → HTTP client, Playwright
```
Benefits: CI fail-fast on cheap stages, clean dependency scopes, parallel execution.

Trade-off: Single module means all levels run together; mitigated by naming conventions and Surefire/Failsafe configuration.

---

## ADR: H2 In-Memory Database for Tests
Decision: Use H2 in-memory database for both unit-style and integration tests; disable Quarkus DevServices.
Context: The production stack uses PostgreSQL, but the test environment has no Docker available for Quarkus DevServices to start a PostgreSQL container.

Rationale:
- H2 provides a zero-infrastructure test database — no Docker, no external process
- `src/test/resources/application.properties` configures H2 with `drop-and-create` schema generation
- Seed data loaded via `import.sql` — same data as production schema
- DevServices explicitly disabled to avoid Docker dependency

Trade-off: H2 dialect differences from PostgreSQL (e.g. JSON operators, window functions). Acceptable for this project's simple queries; a production CI pipeline would use Testcontainers with PostgreSQL.

---

## ADR: Given/When/Then Test Documentation
Decision: All test methods annotated with `// Given`, `// When`, `// Then` comments.
Context: Tests serve as living documentation. The BDD-style structure makes each test's intent immediately clear.

Rationale:
- Given — sets up preconditions (mocks, test data)
- When — executes the action under test
- Then — asserts expected outcomes
- `// When / Then` used for `assertThrows()` patterns where the action and assertion are inseparable

Trade-off: Minor comment noise; justified by readability as living documentation.

---

## ADR: Properties Files for Configuration
Decision: Use `.properties` files for Quarkus configuration.
Context: Quarkus supports both YAML and properties. Properties are the Quarkus convention and provide better grep-ability.

Rationale:
- Flat key-value format is searchable with `grep`
- No indentation-sensitive parsing errors
- Matches Quarkus documentation examples and community conventions

---

## ADR: Domain-Scoped Exception Handling
Decision: Each feature owns its exception handling and error response semantics. No global `ExceptionMapper<Exception>` is used for business validation errors.
Context: A common enterprise pattern is to register a single global `@Provider ExceptionMapper` that catches all exceptions and maps them to a uniform JSON error response. This approach is well-documented in textbooks and framework guides. However, it introduces architectural trade-offs that must be weighed against the domain structure of the application.

This codebase contains multiple business features with distinct validation semantics:

| Feature    | Domain Exceptions                  | Error Semantics                                                                |
|------------|------------------------------------|--------------------------------------------------------------------------------|
| Warehouses | `WarehouseValidationException`     | 5+ distinct validation rules (capacity, BU code, stock, location, feasibility) |
| Stores     | `WebApplicationException` (direct) | CRUD guard clauses (not found, invalid ID, missing name)                       |
| Products   | `WebApplicationException` (direct) | CRUD guard clauses (not found, invalid ID, missing name)                       |

Rationale:
- Separation of Concerns: Each feature controls its own error contract. The warehouse feature maps `WarehouseValidationException` to HTTP 400 with a domain-specific message containing business context (e.g., capacity limits, BU code conflicts). A global handler would need to understand all domain contexts or flatten everything into generic error responses — both violate SoC.
- Single Responsibility Principle: The REST adapter (`WarehouseResourceImpl`) is responsible for translating between HTTP and domain semantics. Externalising this translation to a global handler moves domain knowledge outside the feature boundary.
- Loose Coupling: Each feature can evolve its error handling independently. Adding a new validation rule to warehouses does not require modifying a shared exception handler that all features depend on.
- Decomposition Readiness: In a microservices architecture — the natural evolution of this type of fulfilment system — each service owns its error contract. Domain-scoped handling in the monolith mirrors this target architecture, reducing friction during future decomposition.

Implementation:
- `WarehouseResourceImpl` catches `WarehouseValidationException` and maps to 400 or 404 based on message content
- `StoreResource` and `ProductResource` throw `WebApplicationException` directly with appropriate status codes
- Pre-existing `ErrorMapper` inner classes handle unexpected exceptions (500) with a JSON error body

Trade-off: Slightly more mapping code in each resource; justified by the architectural benefits of feature autonomy and alignment with service decomposition patterns. A global handler is a valid convenience pattern for applications with uniform error semantics — but this codebase intentionally does not have uniform semantics.

Reference: Robert C. Martin, *Clean Architecture* — "The architecture should scream the use cases of the application." Error handling is part of the use case contract, not a cross-cutting infrastructure concern.

---

## ADR: Logging Strategy — Infrastructure-Driven Observability
Decision: Production business logic is not instrumented with application-level logging statements. Observability is addressed through infrastructure mechanisms (health endpoints, API gateway logging, service mesh tracing) and — where required — explicit compliance audit logging.
Context: The assignment scope includes "Logging" as one of several best practices. This ADR documents the deliberate architectural decision to not add `log.info()` / `log.debug()` calls throughout the business logic, and the reasoning behind this approach.

Rationale:
1. Operational Logging — Infrastructure Responsibility

In cloud-native and platform-engineered environments, operational observability is provided by the infrastructure layer, not by application code:

| Concern              | Infrastructure Solution                 | Application Code Required? |
|----------------------|-----------------------------------------|----------------------------|
| Request/response log | API Gateway, Reverse Proxy (e.g. Envoy) | No                         |
| Distributed tracing  | Service Mesh, OpenTelemetry             | No (auto-instrumented)     |
| Health monitoring    | Liveness/Readiness probes               | Minimal (implemented)      |
| Metrics              | Micrometer, Prometheus scraping         | Annotation-driven          |
| Error alerting       | Centralised log aggregation + rules     | Exception propagation only |

This codebase implements SmallRye Health endpoints (`/q/health/live`, `/q/health/ready`) for liveness and readiness probes — the observability mechanism most relevant to a deployment context.

Adding `log.info("Creating warehouse: " + buCode)` to every method:
- Duplicates what the API gateway already captures (request path, parameters, response code)
- Pollutes business logic with infrastructure concerns, reducing readability
- Creates risk of inadvertent PII exposure (customer data, business identifiers in log aggregation systems)

2. Compliance Audit Logging — Explicit, Separate Architecture

Where explicit logging *is* architecturally warranted is for regulatory compliance:

| Tier         | Scope                      | Implementation            | PII Handling        |
|--------------|----------------------------|---------------------------|---------------------|
| Operational  | Request flow, performance  | AOP / interceptors        | No PII permitted    |
| Audit/GDPR   | Data access, modifications | Explicit domain events    | Pseudonymised       |

For an e-commerce fulfilment system operating under GDPR:
- The Right to Erasure (Art. 17) requires audit trails of data access that can themselves be erased
- The Right of Access (Art. 15) requires knowing who accessed what data and when
- Mixing these compliance events into the same `logger.info()` calls as operational logging creates a compliance risk — operational logs are retained indefinitely, audit logs must be erasable

This two-tier separation is an architectural decision that requires domain analysis, not a boilerplate addition. It was deferred as out of scope for this assessment.

3. Assessment Scope and Proportionality
The assignment instructions list: *"Follow the software development best practices such as Code Quality, Coding Standards, Exception Handling & Logging etc."*

The "etc." and the breadth of this list indicate directional guidance, not a pass/fail checklist. Given the 4-hour time constraint, the following prioritisation was applied:

| Delivered (high architectural value)      | Deferred (low assessment value)          |
|-------------------------------------------|------------------------------------------|
| Feature-oriented package restructuring    | Application-level logging statements     |
| 9 Architecture Decision Records           | —                                        |
| CDI transactional events for data safety  | —                                        |
| Hexagonal architecture with port testing  | —                                        |
| CI/CD pipeline with coverage enforcement  | —                                        |
| Health check endpoints (observability)    | —                                        |

Adding `log.info()` calls would have been trivial to implement but would not have demonstrated architectural competence. The items delivered represent decisions that are substantially harder to add retroactively.

Trade-off: No application-level log statements in the current codebase. In a production deployment, operational logging would be added via AOP interceptors (e.g., Quarkus `@Interceptor` or CDI decorators) to avoid polluting business logic, and compliance audit logging would be implemented as explicit domain events with pseudonymisation — following the same CDI event pattern already demonstrated in `LegacyStoreChangeObserver`.

Reference: The Twelve-Factor App, Factor XI (*Logs*) — "A twelve-factor app never concerns itself with routing or storage of its output stream." Application code should emit events; infrastructure should handle aggregation, routing, and retention.

---

## Decisions Deferred (Out of Scope for Assessment)

| Decision                      | Rationale for Deferral                                            |
|-------------------------------|-------------------------------------------------------------------|
| Testcontainers for PostgreSQL | No Docker available; H2 sufficient for assessment                 |
| Multi-module Maven build      | Single-module constraint; logical separation via naming           |
| Security / OAuth              | Assessment focuses on business logic, not infrastructure          |
| Circuit breakers / retries    | Service mesh concern; not relevant for single-service assessment  |
