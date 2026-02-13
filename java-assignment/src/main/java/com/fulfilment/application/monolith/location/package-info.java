/**
 * Location resolution gateway — resolves location identifiers to domain objects.
 *
 * <h2>Role in the architecture</h2>
 * <p>{@link LocationGateway} implements the {@code LocationResolver} port
 * interface defined in the warehouse feature. It provides an in-memory
 * catalogue of valid fulfilment locations with their capacity constraints
 * (max warehouses, max total capacity).</p>
 *
 * <h2>Port implementation</h2>
 * <p>The warehouse use cases ({@code CreateWarehouseUseCase},
 * {@code ReplaceWarehouseUseCase}) depend on the {@code LocationResolver}
 * interface, not on this concrete gateway. This enables:</p>
 * <ul>
 *   <li>Unit testing with Mockito stubs (no gateway needed)</li>
 *   <li>Future replacement with a remote location service without
 *       modifying business logic</li>
 * </ul>
 *
 * @see <a href="../../../../../../../Docs/ADR-Architecture-Decisions.md">
 *      Docs/ADR-Architecture-Decisions.md</a>
 */
package com.fulfilment.application.monolith.location;
