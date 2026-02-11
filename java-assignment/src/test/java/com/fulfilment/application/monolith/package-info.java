/**
 * Test root for the fulfilment monolith application.
 *
 * <h2>Feature-oriented project structure</h2>
 * <p>Both source and test packages are organised by <strong>business feature</strong>,
 * not by technical layer (see {@code Docs/ADR-Feature-Oriented-Project.md}):</p>
 * <pre>
 *   warehouses/           &rarr; the warehouse feature (model, ports, persistence, API)
 *   warehouses/create/    &rarr; "create a warehouse" capability
 *   warehouses/archive/   &rarr; "archive a warehouse" capability
 *   warehouses/replace/   &rarr; "replace a warehouse" capability
 *   products/             &rarr; the product feature
 *   stores/               &rarr; the store feature
 *   location/             &rarr; the location gateway
 * </pre>
 *
 * <h2>Test strategy overview</h2>
 * <p>Tests follow the <strong>test pyramid</strong>:</p>
 * <ol>
 *   <li><strong>Unit tests</strong> ({@code *Test.java}) &mdash; fast, isolated,
 *       Mockito-based. Cover business use cases and pure adapters.
 *       Packages: {@code warehouses/create}, {@code warehouses/archive},
 *       {@code warehouses/replace}, {@code location}.</li>
 *   <li><strong>Integration tests</strong> ({@code *IT.java} / {@code *Test.java}
 *       with {@code @QuarkusTest}) &mdash; boot the container with H2 in-memory DB.
 *       Validate wiring and HTTP contracts.
 *       Packages: {@code warehouses} (root), {@code products}.</li>
 *   <li><strong>E2E tests</strong> (IntelliJ HTTP Request files in
 *       {@code src/test/resources/e2e/}) &mdash; manual smoke tests against a
 *       running instance. See {@code E2E-Warehouse.http}, {@code E2E-Store.http}.</li>
 * </ol>
 *
 * <h2>Single-module constraint</h2>
 * <p>This assessment project uses a single Maven module. In production, each test
 * level would live in its own module to enforce structural separation:</p>
 * <pre>
 *   warehouse-domain/src/test          &rarr; unit tests
 *   warehouse-integration-tests/       &rarr; @QuarkusTest, test-containers
 *   warehouse-e2e-tests/               &rarr; HTTP client tests, Playwright, etc.
 * </pre>
 * <p>Benefits: fast CI feedback (fail-fast on unit), clean dependency scopes, and
 * parallel execution of independent test stages.</p>
 *
 * <h2>Code style: explicit types over {@code var}</h2>
 * <p>All test code uses explicit type declarations instead of {@code var}.
 * This deliberate choice improves processing by AI coding agents &mdash; the
 * type context on every declaration provides stronger reinforcement for code
 * understanding, generation and review. It also helps human reviewers immediately
 * see the type flowing through each test step without IDE hover support.</p>
 */
package com.fulfilment.application.monolith;
