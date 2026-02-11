/**
 * Unit tests for the <strong>archive warehouse</strong> business capability.
 *
 * <h2>Test classification: UNIT</h2>
 * <p>Verifies archival logic: existing active warehouse is archived, non-existent
 * and already-archived warehouses are rejected. Uses Mockito stubs, runs without
 * a container in milliseconds.</p>
 *
 * <h2>Code style: explicit types over {@code var}</h2>
 * <p>Explicit type declarations are preferred over {@code var} to maximise
 * readability for human reviewers and AI coding agents.</p>
 */
package com.fulfilment.application.monolith.warehouses.archive;
