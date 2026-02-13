/**
 * "Create a warehouse" business capability.
 *
 * <h2>Port and use case</h2>
 * <ul>
 *   <li>{@link CreateWarehouseOperation} — port interface defining the creation contract</li>
 *   <li>{@link CreateWarehouseUseCase} — implements 5 validation rules:
 *     <ol>
 *       <li>Business unit code uniqueness</li>
 *       <li>Location existence (via {@code LocationResolver})</li>
 *       <li>Maximum warehouses per location</li>
 *       <li>Total capacity at location</li>
 *       <li>Stock does not exceed capacity</li>
 *     </ol>
 *   </li>
 * </ul>
 *
 * <h2>Testability</h2>
 * <p>The use case depends only on port interfaces ({@code WarehouseStore},
 * {@code LocationResolver}), enabling fast unit testing with Mockito stubs —
 * no container, no database, millisecond execution.</p>
 *
 * @see <a href="../../../../../../../../Docs/ADR-Architecture-Decisions.md">
 *      Docs/ADR-Architecture-Decisions.md</a>
 */
package com.fulfilment.application.monolith.warehouses.create;
