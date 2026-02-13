/**
 * Warehouse feature — domain model, ports, persistence adapter, and REST adapter.
 *
 * <h2>Architecture: hexagonal (ports and adapters)</h2>
 * <p>The warehouse feature uses a hexagonal architecture where business rules
 * are protected from infrastructure concerns:</p>
 * <table>
 *   <tr><th>Component</th><th>Role</th><th>Example</th></tr>
 *   <tr><td>Port (interface)</td><td>Defines what the domain needs</td>
 *       <td>{@link WarehouseStore}, {@code CreateWarehouseOperation}</td></tr>
 *   <tr><td>Use case</td><td>Implements business rules</td>
 *       <td>{@code CreateWarehouseUseCase} (5 validations)</td></tr>
 *   <tr><td>Adapter</td><td>Connects domain to infrastructure</td>
 *       <td>{@link WarehouseRepository} (JPA), {@link WarehouseResourceImpl} (REST)</td></tr>
 * </table>
 *
 * <h2>Package layout</h2>
 * <p>Sub-packages are named by <strong>business operation</strong>, not technical role:</p>
 * <pre>
 *   warehouses/           — shared: domain model, ports, persistence, REST adapter
 *   warehouses/create/    — "create a warehouse" use case + port interface
 *   warehouses/archive/   — "archive a warehouse" use case + port interface
 *   warehouses/replace/   — "replace a warehouse" use case + port interface
 * </pre>
 *
 * <h2>Exception handling: domain-scoped</h2>
 * <p>{@link WarehouseValidationException} carries domain-specific error messages
 * (capacity limits, BU code conflicts, stock validation). The REST adapter
 * ({@link WarehouseResourceImpl}) maps these to HTTP 400/404 — each feature
 * owns its error contract. See ADR: Domain-Scoped Exception Handling in
 * {@code Docs/ADR-Architecture-Decisions.md}.</p>
 *
 * @see <a href="../../../../../../../Docs/ADR-Architecture-Decisions.md">
 *      Docs/ADR-Architecture-Decisions.md</a>
 */
package com.fulfilment.application.monolith.warehouses;
