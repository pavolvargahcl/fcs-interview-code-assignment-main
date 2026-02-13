/**
 * Store feature — CRUD operations with CDI event-driven legacy system synchronisation.
 *
 * <h2>Architecture: Active Record + CDI events</h2>
 * <p>{@link Store} extends {@code PanacheEntity} (Active Record pattern) for
 * straightforward CRUD. The interesting architectural element is the
 * <strong>post-commit legacy sync</strong>:</p>
 * <ol>
 *   <li>{@link StoreResource} persists the store and fires a
 *       {@link StoreChangeEvent} via CDI {@code Event}</li>
 *   <li>{@link LegacyStoreChangeObserver} observes with
 *       {@code @Observes(during = TransactionPhase.AFTER_SUCCESS)}</li>
 *   <li>The observer delegates to {@link LegacyStoreManagerGateway} —
 *       <strong>only after the database transaction commits successfully</strong></li>
 * </ol>
 * <p>This guarantees the legacy system is never notified of a change that
 * was rolled back. See ADR: CDI Events for Post-Commit Legacy System Sync
 * in {@code Docs/ADR-Architecture-Decisions.md}.</p>
 *
 * <h2>Exception handling</h2>
 * <p>{@link StoreResource} throws {@code WebApplicationException} directly
 * with domain-appropriate status codes (404, 422). This is the domain-scoped
 * exception handling pattern — see ADR: Domain-Scoped Exception Handling
 * in {@code Docs/ADR-Architecture-Decisions.md}.</p>
 *
 * @see <a href="../../../../../../../Docs/ADR-Architecture-Decisions.md">
 *      Docs/ADR-Architecture-Decisions.md</a>
 */
package com.fulfilment.application.monolith.stores;
