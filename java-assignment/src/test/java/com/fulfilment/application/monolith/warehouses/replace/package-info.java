/**
 * Unit tests for the <strong>replace warehouse</strong> business capability.
 *
 * <h2>Test classification: UNIT</h2>
 * <p>Verifies the replace-in-place workflow: old warehouse archived, new warehouse
 * created with matching BU code. Rejection cases: warehouse not found, capacity
 * cannot accommodate stock, stock mismatch, invalid location. Uses Mockito stubs,
 * runs without a container in milliseconds.</p>
 *
 * <h2>Code style: explicit types over {@code var}</h2>
 * <p>Explicit type declarations are preferred over {@code var} to maximise
 * readability for human reviewers and AI coding agents.</p>
 */
package com.fulfilment.application.monolith.warehouses.replace;
