package com.fulfilment.application.monolith.warehouses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DbWarehouseTest {

  @Test
  void shouldConvertToWarehouseWithAllFields() {
    // Given
    DbWarehouse db = new DbWarehouse();
    db.id = 1L;
    db.businessUnitCode = "MWH.001";
    db.location = "AMSTERDAM-001";
    db.capacity = 50;
    db.stock = 20;
    db.createdAt = LocalDateTime.of(2024, 7, 1, 10, 0);
    db.archivedAt = LocalDateTime.of(2024, 12, 1, 15, 30);

    // When
    Warehouse warehouse = db.toWarehouse();

    // Then
    assertNotNull(warehouse);
    assertEquals("MWH.001", warehouse.businessUnitCode);
    assertEquals("AMSTERDAM-001", warehouse.location);
    assertEquals(50, warehouse.capacity);
    assertEquals(20, warehouse.stock);
    assertEquals(LocalDateTime.of(2024, 7, 1, 10, 0), warehouse.createdAt);
    assertEquals(LocalDateTime.of(2024, 12, 1, 15, 30), warehouse.archivedAt);
  }

  @Test
  void shouldConvertToWarehouseWithNullTimestamps() {
    // Given
    DbWarehouse db = new DbWarehouse();
    db.id = 2L;
    db.businessUnitCode = "MWH.002";
    db.location = "ZWOLLE-001";
    db.capacity = 30;
    db.stock = 10;
    db.createdAt = null;
    db.archivedAt = null;

    // When
    Warehouse warehouse = db.toWarehouse();

    // Then
    assertNotNull(warehouse);
    assertEquals("MWH.002", warehouse.businessUnitCode);
    assertEquals("ZWOLLE-001", warehouse.location);
    assertNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
  }

  @Test
  void shouldCreateFromWarehouseWithAllFields() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.003";
    warehouse.location = "TILBURG-001";
    warehouse.capacity = 60;
    warehouse.stock = 25;
    warehouse.createdAt = LocalDateTime.of(2024, 8, 15, 9, 0);
    warehouse.archivedAt = LocalDateTime.of(2024, 11, 20, 18, 0);

    // When
    DbWarehouse db = DbWarehouse.fromWarehouse(warehouse);

    // Then
    assertNotNull(db);
    assertEquals("MWH.003", db.businessUnitCode);
    assertEquals("TILBURG-001", db.location);
    assertEquals(60, db.capacity);
    assertEquals(25, db.stock);
    assertEquals(LocalDateTime.of(2024, 8, 15, 9, 0), db.createdAt);
    assertEquals(LocalDateTime.of(2024, 11, 20, 18, 0), db.archivedAt);
  }

  @Test
  void shouldCreateFromWarehouseWithNullTimestamps() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.004";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 40;
    warehouse.stock = 0;
    warehouse.createdAt = null;
    warehouse.archivedAt = null;

    // When
    DbWarehouse db = DbWarehouse.fromWarehouse(warehouse);

    // Then
    assertNotNull(db);
    assertEquals("MWH.004", db.businessUnitCode);
    assertNull(db.createdAt);
    assertNull(db.archivedAt);
    assertNull(db.id);
  }

  @Test
  void shouldPreserveDataThroughRoundTrip() {
    // Given
    Warehouse original = new Warehouse();
    original.businessUnitCode = "ROUND.001";
    original.location = "EINDHOVEN-001";
    original.capacity = 45;
    original.stock = 15;
    original.createdAt = LocalDateTime.of(2024, 6, 1, 12, 0);
    original.archivedAt = null;

    // When
    DbWarehouse db = DbWarehouse.fromWarehouse(original);
    Warehouse roundTripped = db.toWarehouse();

    // Then
    assertEquals(original.businessUnitCode, roundTripped.businessUnitCode);
    assertEquals(original.location, roundTripped.location);
    assertEquals(original.capacity, roundTripped.capacity);
    assertEquals(original.stock, roundTripped.stock);
    assertEquals(original.createdAt, roundTripped.createdAt);
    assertEquals(original.archivedAt, roundTripped.archivedAt);
  }
}
