/**
 * "Archive a warehouse" business capability.
 *
 * <h2>Port and use case</h2>
 * <ul>
 *   <li>{@link ArchiveWarehouseOperation} — port interface defining the archival contract</li>
 *   <li>{@link ArchiveWarehouseUseCase} — validates the warehouse exists and is not
 *       already archived, then sets {@code archivedAt} timestamp and persists via
 *       {@code WarehouseStore}</li>
 * </ul>
 *
 * <h2>Soft delete pattern</h2>
 * <p>Archival uses a soft-delete approach: the {@code archivedAt} field marks
 * the warehouse as inactive without removing the database record. Active
 * warehouse queries filter on {@code archivedAt IS NULL}.</p>
 *
 * @see <a href="../../../../../../../../Docs/ADR-Architecture-Decisions.md">
 *      Docs/ADR-Architecture-Decisions.md</a>
 */
package com.fulfilment.application.monolith.warehouses.archive;
