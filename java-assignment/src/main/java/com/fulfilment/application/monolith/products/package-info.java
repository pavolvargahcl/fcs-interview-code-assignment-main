/**
 * Product feature — simple CRUD with the PanacheRepository pattern.
 *
 * <h2>Data access pattern</h2>
 * <p>{@link ProductRepository} implements {@code PanacheRepository<Product>},
 * providing a clean separation between the REST resource and persistence.
 * This pattern was chosen over Active Record ({@code PanacheEntity}) to enable
 * unit testing of {@link ProductResource} with Mockito — the repository is
 * injected via constructor and can be mocked without a running container.</p>
 *
 * <h2>Intentionally varied patterns across features</h2>
 * <p>The codebase deliberately uses different data access patterns per feature,
 * selected by complexity (see root package-info and
 * {@code Docs/ADR-Architecture-Decisions.md}):</p>
 * <ul>
 *   <li><strong>Products</strong>: PanacheRepository — simple CRUD, repository testable</li>
 *   <li><strong>Stores</strong>: PanacheEntity (Active Record) — pre-existing pattern</li>
 *   <li><strong>Warehouses</strong>: Hexagonal (ports &amp; adapters) — complex domain logic</li>
 * </ul>
 *
 * @see <a href="../../../../../../../Docs/ADR-Architecture-Decisions.md">
 *      Docs/ADR-Architecture-Decisions.md</a>
 */
package com.fulfilment.application.monolith.products;
