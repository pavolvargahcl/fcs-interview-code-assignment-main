# Questions
Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
The codebase uses three distinct data access patterns:

1. Products: PanacheRepository pattern — the resource injects ProductRepository which
   encapsulates all Panache operations. Clean separation between resource and data access.

2. Stores: Active Record (PanacheEntity) — Store extends PanacheEntity and the resource
   calls static methods directly (Store.findById(), store.persist()). Convenient but
   couples the entity to the persistence framework, making isolated unit testing harder.

3. Warehouses: Hexagonal/Ports-and-Adapters — the domain defines a WarehouseStore port
   interface, and WarehouseRepository implements it as an adapter. Business logic in
   use cases depends only on the port abstraction, not on Panache directly.

In a feature-oriented codebase, each feature is autonomous and chooses the data access
pattern appropriate to its complexity. Pattern uniformity across features is not a goal
— a developer working on "stores" does not need to know how "warehouses" accesses data.
The right question is: does each feature's chosen pattern serve that feature well?

- Products (PanacheRepository): Appropriate. Simple CRUD, no business rules. The
  repository gives clean separation without over-engineering. No change needed.

- Warehouses (Hexagonal): Appropriate. Complex domain with five validation rules,
  three distinct operations, and location capacity constraints. Port interfaces keep
  business logic decoupled from infrastructure and enable fast, isolated unit tests
  with Mockito stubs. No change needed.

- Stores (Active Record): This is the one I would refactor — not for cross-feature
  consistency, but because Active Record violates SRP within the stores feature itself.
  The Store entity is simultaneously a data structure and its own persistence mechanism.
  This makes isolated unit testing impractical (static methods like Store.findById()
  cannot be mocked without framework hacks) and couples the entity to PanacheEntity
  inheritance. I would extract a StoreRepository (PanacheRepository pattern) so that
  the resource depends on an injectable, mockable repository — the same reasoning that
  makes ProductRepository work well for the products feature.

I would NOT refactor Products or Stores to hexagonal — that would be over-engineering
for simple CRUD features with no business rules. The hexagonal approach earns its
complexity only when there is genuine domain logic to protect from infrastructure.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
This is not a binary "OpenAPI-first vs code-first" decision. The enterprise answer
is a hybrid strategy: centralised contract governance with selective code generation.

Centralised governance (the non-negotiable):
Every API — regardless of implementation approach — must be governed by a central
API Management platform (e.g. Apigee, Kong, AWS API Gateway). This is the single
source of truth for API design, discoverability, versioning, and standard enforcement.
The OpenAPI spec lives here, not in individual service repos.

The OpenAPI tooling constraint:
OpenAPI tooling struggles with cross-file $ref resolution. This forces teams into
monolithic "mega-files" that violate SRP, create merge conflicts in multi-team
environments, and expose the entire API surface in one document. This is a real
limitation that must inform when to use code generation.

When to use code generation (targeted):
Server stub generation works well for standard, boilerplate-heavy microservices
where speed is prioritised over custom architecture. In this project, the Warehouse
API is a good fit — the spec was provided, the generated interface enforces the
contract at compile time, and WarehouseResourceImpl simply implements it.

When to implement manually (complex domains):
For core business domains with rich validation, orchestration, or non-standard
patterns, teams should abandon stub generation and implement routing and business
logic manually. This retains full architectural flexibility. Contract drift is then
prevented not by code generation but by a rigorous automated testing pyramid:
unit tests for business logic, integration tests for HTTP contract verification,
and E2E tests (like IntelliJ HTTP Request files) for full-stack validation.

Applied to this project:
- Warehouse: OpenAPI-generated interface — correct choice, spec was provided,
  contract enforcement at compile time.
- Products/Stores: Hand-coded JAX-RS — correct choice for simple CRUD with no
  external consumers. Adding SmallRye OpenAPI annotations would auto-generate
  the spec from code, giving documentation without the generation overhead.

The key insight: the contract is always centralised and authoritative. The
implementation strategy (generated vs manual) is a per-service decision based
on complexity, team autonomy, and the specific OpenAPI tooling constraints.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
The test pyramid is not just a prioritisation model — it must be enforced structurally
through module separation. Each test level has different speed characteristics, dependency
requirements, and execution contexts. Mixing them in one module erodes the fast feedback
loop that makes unit tests valuable.

Structural separation (production multi-module build):

  feature-module/src/test/            → unit tests only (Mockito, no container)
  feature-integration-tests/          → @QuarkusTest, Testcontainers, REST-assured
  feature-e2e-tests/                  → HTTP client tests, IntelliJ .http files, Playwright

Why separate modules — not just separate naming conventions:
- Speed enforcement: unit tests run in seconds; integration tests boot a container.
  If they share a module, `mvn test` always pays the container cost, destroying the
  fast feedback loop. Separate modules let CI run unit → integration → E2E as
  independent, parallelisable stages with early fail-fast.
- Dependency isolation: integration modules pull in Testcontainers, H2, REST-assured.
  These never leak into the production classpath or the unit test classpath.
- Team autonomy: a developer fixing a validation rule runs only unit tests locally.
  Integration tests run in CI or on explicit request.

In this single-module assessment project, we approximate this with naming conventions
(*Test for unit, *IT for integration) and Surefire/Failsafe plugin configuration.
E2E tests are IntelliJ HTTP Request files (src/test/resources/e2e/) with JavaScript
assertions, runnable against a live Quarkus instance.

Priority 1 — Unit tests for business logic (base of pyramid, highest ROI):
Focus on the warehouse use cases (Create, Archive, Replace) since they contain the
critical validation rules. These tests use mocked ports, run without a container
in milliseconds, and live in the same module as the production code they test.
Each validation path has both a positive and negative test case. Feature-oriented
test structure means the tests sit alongside their capability: warehouses/create/,
warehouses/archive/, warehouses/replace/.

Priority 2 — Integration tests for API contracts:
REST-assured tests with @QuarkusTest verify the full HTTP → CDI → JPA stack: routing,
serialisation, transaction boundaries, and database queries. In a multi-module build,
these live in a dedicated integration-tests module. Focus on the happy path for each
endpoint plus key error scenarios (404, 400).

Priority 3 — E2E smoke tests:
IntelliJ HTTP Request files (.http) provide human-readable, executable API smoke tests
against a running instance. These serve as both documentation and a quick manual
validation tool. In CI, they can be executed via the IntelliJ HTTP Client CLI plugin.

What I would skip (given constraints):
- UI/frontend tests (the AngularJS UI is secondary to the API).
- Performance tests (premature for a PoC).
- Exhaustive mutation testing (diminishing returns).

Ensuring coverage remains effective over time:
- Integrate JaCoCo with a minimum coverage threshold (e.g., 80% line coverage on
  business logic packages). Apply it to the unit test module only — integration
  test coverage is a bonus, not a gate.
- Enforce that every PR touching business logic includes corresponding test updates.
- Review test quality periodically — high coverage with weak assertions is misleading.
  Focus on meaningful assertions that verify behaviour, not just that code executes.
- Given/When/Then structure in every test method — tests are living documentation.
```
