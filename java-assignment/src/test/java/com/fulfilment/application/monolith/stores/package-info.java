/**
 * Tests for the store feature — CRUD operations, CDI event-driven legacy sync,
 * and the legacy gateway adapter.
 *
 * <h2>Test classification</h2>
 * <ul>
 *   <li><strong>Unit tests</strong> ({@code LegacyStoreChangeObserverTest},
 *       {@code LegacyStoreManagerGatewayTest}) &mdash; Mockito-based, millisecond
 *       execution, no container. Verify event delegation and gateway file I/O
 *       in isolation.</li>
 *   <li><strong>Integration tests</strong> ({@code StoreResourceTest}) &mdash;
 *       {@code @QuarkusTest} with REST-assured and H2 in-memory database.
 *       Validates the full HTTP &rarr; CDI &rarr; JPA stack for all CRUD
 *       operations including CDI event firing for legacy system sync.</li>
 * </ul>
 *
 * <h2>Why {@code StoreResource} uses integration tests</h2>
 * <p>{@link com.fulfilment.application.monolith.stores.Store} extends
 * {@code PanacheEntity} (Active Record pattern), exposing persistence as
 * static methods ({@code Store.findById()}, {@code Store.listAll()}).
 * These methods are Quarkus bytecode-enhanced at build time and are not
 * available in plain JUnit without the Quarkus container. Therefore,
 * {@code StoreResource} is tested via {@code @QuarkusTest} rather than
 * Mockito-based unit tests.</p>
 *
 * <h2>Architecture: CDI event-driven post-commit sync</h2>
 * <p>The store feature demonstrates transactional safety via CDI events:
 * {@code StoreResource} fires {@code StoreChangeEvent} after persist;
 * {@code LegacyStoreChangeObserver} observes with
 * {@code @Observes(during = TransactionPhase.AFTER_SUCCESS)}, guaranteeing
 * the legacy system is notified only after a successful database commit.
 * See {@code Docs/ADR-Architecture-Decisions.md}.</p>
 */
package com.fulfilment.application.monolith.stores;
