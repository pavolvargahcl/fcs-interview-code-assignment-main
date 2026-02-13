/**
 * Fulfilment Warehouse Application — monolith root package.
 *
 * <h2>Architecture overview</h2>
 * <p>This application manages the lifecycle of warehouses, stores, and products
 * within a fulfilment network. The codebase is organised by <strong>business
 * feature</strong>, not by technical layer:</p>
 * <pre>
 *   warehouses/           — warehouse feature (hexagonal architecture)
 *   warehouses/create/    — "create a warehouse" use case
 *   warehouses/archive/   — "archive a warehouse" use case
 *   warehouses/replace/   — "replace a warehouse" use case
 *   stores/               — store feature (Active Record, CDI event-driven legacy sync)
 *   products/             — product feature (repository pattern)
 *   location/             — location resolution gateway
 * </pre>
 *
 * <h2>Key architectural decisions</h2>
 * <p>All architectural decisions are documented as formal ADRs in
 * {@code Docs/ADR-Architecture-Decisions.md}, including:</p>
 * <ul>
 *   <li>Feature-oriented package structure (vs. technical-layer packages)</li>
 *   <li>Hexagonal architecture within the warehouse feature</li>
 *   <li>CDI events for post-commit legacy system synchronisation</li>
 *   <li>Domain-scoped exception handling (vs. global ExceptionMapper)</li>
 *   <li>Logging strategy — infrastructure-driven observability</li>
 *   <li>Test pyramid with logical separation</li>
 *   <li>OpenAPI-first code generation for the warehouse API</li>
 * </ul>
 *
 * <h2>Data access patterns — intentionally varied</h2>
 * <p>Each feature selects its data access pattern based on its own complexity:</p>
 * <table>
 *   <tr><th>Feature</th><th>Pattern</th><th>Rationale</th></tr>
 *   <tr><td>Warehouses</td><td>Hexagonal (ports &amp; adapters)</td>
 *       <td>Complex domain logic with 5+ validation rules; testability critical</td></tr>
 *   <tr><td>Products</td><td>PanacheRepository</td>
 *       <td>Simple CRUD; repository injected into resource for testability</td></tr>
 *   <tr><td>Stores</td><td>PanacheEntity (Active Record)</td>
 *       <td>Pre-existing pattern; lightweight for simple CRUD</td></tr>
 * </table>
 *
 * @see <a href="../../../../../../Docs/ADR-Architecture-Decisions.md">
 *      Docs/ADR-Architecture-Decisions.md</a>
 */
package com.fulfilment.application.monolith;
