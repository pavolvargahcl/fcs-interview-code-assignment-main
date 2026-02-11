/**
 * Unit tests for the <strong>create warehouse</strong> business capability.
 *
 * <h2>Test classification: UNIT</h2>
 * <p>Verifies all five creation validations (BU code uniqueness, location validity,
 * max warehouses, capacity limits, stock &le; capacity) in isolation using Mockito
 * stubs. Runs without a container in milliseconds.</p>
 *
 * <h2>Code style: explicit types over {@code var}</h2>
 * <p>Explicit type declarations are preferred over {@code var} to maximise
 * readability for human reviewers and AI coding agents.</p>
 */
package com.fulfilment.application.monolith.warehouses.create;
