/**
 * Tests for the warehouse feature.
 *
 * <h2>Feature-oriented structure</h2>
 * <p>Both source and test packages follow a <strong>feature-oriented</strong> layout
 * (see {@code Docs/ADR-Feature-Oriented-Project.md}). Packages are organised by
 * business capability, not by technical layer:</p>
 * <pre>
 *   warehouses/           &rarr; shared model, ports, persistence, REST adapter
 *   warehouses/create/    &rarr; "create a warehouse" use case + port
 *   warehouses/archive/   &rarr; "archive a warehouse" use case + port
 *   warehouses/replace/   &rarr; "replace a warehouse" use case + port
 * </pre>
 * <p>A developer working on "replacing a warehouse" navigates straight to
 * {@code replace/} &mdash; both the production code and its tests live there.</p>
 *
 * <h2>Integration test at feature root</h2>
 * <p>{@link com.fulfilment.application.monolith.warehouses.WarehouseEndpointIT}
 * lives at the feature root because it exercises the entire warehouse HTTP
 * contract end-to-end (list, create, archive, replace), spanning all
 * sub-capabilities.</p>
 *
 * <h2>Module placement rationale</h2>
 * <p>In a production codebase, integration and E2E tests belong in separate Maven
 * modules to enforce the test pyramid structurally and keep CI feedback fast.
 * This single-module assessment uses naming conventions ({@code *Test} for unit,
 * {@code *IT} for integration) and Surefire/Failsafe configuration as a
 * pragmatic substitute.</p>
 *
 * <h2>Code style: explicit types over {@code var}</h2>
 * <p>Explicit type declarations are preferred over {@code var} to maximise
 * readability for human reviewers and AI coding agents &mdash; the type context
 * on every line provides stronger reinforcement for code understanding,
 * generation and review.</p>
 */
package com.fulfilment.application.monolith.warehouses;
