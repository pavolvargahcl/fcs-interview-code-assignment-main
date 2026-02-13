/**
 * "Replace a warehouse" business capability.
 *
 * <h2>Port and use case</h2>
 * <ul>
 *   <li>{@link ReplaceWarehouseOperation} — port interface defining the replacement contract</li>
 *   <li>{@link ReplaceWarehouseUseCase} — orchestrates:
 *     <ol>
 *       <li>Locate existing warehouse by business unit code</li>
 *       <li>Validate new capacity accommodates existing stock</li>
 *       <li>Validate stock quantities match (continuity guarantee)</li>
 *       <li>Validate new location exists and has capacity</li>
 *       <li>Archive the existing warehouse (soft delete)</li>
 *       <li>Create the replacement warehouse</li>
 *     </ol>
 *   </li>
 * </ul>
 *
 * <h2>Transactional integrity</h2>
 * <p>The archive-then-create sequence executes within a single transaction
 * (managed by the REST adapter's {@code @Transactional} annotation), ensuring
 * atomicity: either both operations succeed or neither does.</p>
 *
 * @see <a href="../../../../../../../../Docs/ADR-Architecture-Decisions.md">
 *      Docs/ADR-Architecture-Decisions.md</a>
 */
package com.fulfilment.application.monolith.warehouses.replace;
