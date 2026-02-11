/**
 * Integration tests for the pre-existing product REST endpoint.
 *
 * <h2>Test classification: INTEGRATION</h2>
 * <p>Uses {@code @QuarkusTest} to boot the full container and validate the product
 * CRUD lifecycle through HTTP. Kept in-process (not {@code @QuarkusIntegrationTest})
 * to avoid a separate JAR launch and external database requirement.</p>
 *
 * <h2>Module placement rationale</h2>
 * <p>Same rationale as the warehouse integration tests &mdash; in a real multi-module
 * project this would reside in a dedicated {@code *-integration-tests} module to
 * enforce the test pyramid structurally. See
 * {@link com.fulfilment.application.monolith.warehouses.adapters.restapi} package-info
 * for full reasoning.</p>
 *
 * <h2>Code style: explicit types over {@code var}</h2>
 * <p>Explicit type declarations are preferred over {@code var} to maximise readability
 * for human reviewers and AI coding agents &mdash; the additional type context on
 * every line provides stronger reinforcement for code understanding, generation
 * and review.</p>
 */
package com.fulfilment.application.monolith.products;
