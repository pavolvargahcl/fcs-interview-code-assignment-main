package com.fulfilment.application.monolith.warehouses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class WarehouseRepositoryTest {

  private WarehouseRepository repository;

  @SuppressWarnings("unchecked")
  private final PanacheQuery<DbWarehouse> panacheQuery = mock(PanacheQuery.class);

  @BeforeEach
  void setUp() {
    repository = Mockito.spy(new WarehouseRepository());
  }

  // --- getAll ---

  @Test
  void shouldReturnAllActiveWarehouses() {
    // Given
    DbWarehouse db1 = buildDbWarehouse("MWH.001", "AMSTERDAM-001", 50, 20);
    DbWarehouse db2 = buildDbWarehouse("MWH.002", "ZWOLLE-001", 30, 10);
    when(panacheQuery.list()).thenReturn(List.of(db1, db2));
    Mockito.doReturn(panacheQuery).when(repository).find("archivedAt is null");

    // When
    List<Warehouse> result = repository.getAll();

    // Then
    assertEquals(2, result.size());
    assertEquals("MWH.001", result.get(0).businessUnitCode);
    assertEquals("MWH.002", result.get(1).businessUnitCode);
  }

  @Test
  void shouldReturnEmptyListWhenNoActiveWarehouses() {
    // Given
    when(panacheQuery.list()).thenReturn(Collections.emptyList());
    Mockito.doReturn(panacheQuery).when(repository).find("archivedAt is null");

    // When
    List<Warehouse> result = repository.getAll();

    // Then
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  // --- create ---

  @Test
  void shouldPersistNewWarehouse() {
    // Given
    Warehouse warehouse = buildWarehouse("NEW.001", "AMSTERDAM-001", 40, 10);
    doNothing().when(repository).persist(any(DbWarehouse.class));

    // When
    repository.create(warehouse);

    // Then
    ArgumentCaptor<DbWarehouse> captor = ArgumentCaptor.forClass(DbWarehouse.class);
    verify(repository).persist(captor.capture());
    DbWarehouse persisted = captor.getValue();
    assertEquals("NEW.001", persisted.businessUnitCode);
    assertEquals("AMSTERDAM-001", persisted.location);
    assertEquals(40, persisted.capacity);
    assertEquals(10, persisted.stock);
  }

  // --- update ---

  @Test
  void shouldUpdateExistingWarehouseFields() {
    // Given
    DbWarehouse existing = buildDbWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    when(panacheQuery.firstResult()).thenReturn(existing);
    Mockito.doReturn(panacheQuery).when(repository)
        .find("businessUnitCode = ?1 and archivedAt is null", "MWH.001");

    Warehouse updated = buildWarehouse("MWH.001", "AMSTERDAM-001", 50, 20);
    updated.archivedAt = LocalDateTime.of(2024, 12, 1, 0, 0);

    // When
    repository.update(updated);

    // Then
    assertEquals("AMSTERDAM-001", existing.location);
    assertEquals(50, existing.capacity);
    assertEquals(20, existing.stock);
    assertEquals(LocalDateTime.of(2024, 12, 1, 0, 0), existing.archivedAt);
  }

  @Test
  void shouldDoNothingWhenUpdatingNonExistentWarehouse() {
    // Given
    when(panacheQuery.firstResult()).thenReturn(null);
    Mockito.doReturn(panacheQuery).when(repository)
        .find("businessUnitCode = ?1 and archivedAt is null", "NON.EXISTENT");

    Warehouse updated = buildWarehouse("NON.EXISTENT", "AMSTERDAM-001", 50, 20);

    // When
    repository.update(updated);

    // Then — no exception, graceful no-op
  }

  // --- remove ---

  @Test
  void shouldDeleteExistingWarehouse() {
    // Given
    DbWarehouse existing = buildDbWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    when(panacheQuery.firstResult()).thenReturn(existing);
    Mockito.doReturn(panacheQuery).when(repository)
        .find("businessUnitCode = ?1 and archivedAt is null", "MWH.001");
    doNothing().when(repository).delete(any(DbWarehouse.class));

    Warehouse toRemove = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);

    // When
    repository.remove(toRemove);

    // Then
    verify(repository).delete(existing);
  }

  @Test
  void shouldDoNothingWhenRemovingNonExistentWarehouse() {
    // Given
    when(panacheQuery.firstResult()).thenReturn(null);
    Mockito.doReturn(panacheQuery).when(repository)
        .find("businessUnitCode = ?1 and archivedAt is null", "NON.EXISTENT");

    Warehouse toRemove = buildWarehouse("NON.EXISTENT", "ZWOLLE-001", 30, 10);

    // When
    repository.remove(toRemove);

    // Then — no exception, graceful no-op
  }

  // --- findByBusinessUnitCode ---

  @Test
  void shouldFindWarehouseByBusinessUnitCode() {
    // Given
    DbWarehouse db = buildDbWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    when(panacheQuery.firstResult()).thenReturn(db);
    Mockito.doReturn(panacheQuery).when(repository)
        .find("businessUnitCode = ?1 and archivedAt is null", "MWH.001");

    // When
    Warehouse result = repository.findByBusinessUnitCode("MWH.001");

    // Then
    assertNotNull(result);
    assertEquals("MWH.001", result.businessUnitCode);
    assertEquals("ZWOLLE-001", result.location);
  }

  @Test
  void shouldReturnNullWhenBusinessUnitCodeNotFound() {
    // Given
    when(panacheQuery.firstResult()).thenReturn(null);
    Mockito.doReturn(panacheQuery).when(repository)
        .find("businessUnitCode = ?1 and archivedAt is null", "NON.EXISTENT");

    // When
    Warehouse result = repository.findByBusinessUnitCode("NON.EXISTENT");

    // Then
    assertNull(result);
  }

  // --- findActiveByLocation ---

  @Test
  void shouldFindActiveWarehousesByLocation() {
    // Given
    DbWarehouse db1 = buildDbWarehouse("MWH.001", "AMSTERDAM-001", 50, 20);
    DbWarehouse db2 = buildDbWarehouse("MWH.002", "AMSTERDAM-001", 30, 10);
    when(panacheQuery.list()).thenReturn(List.of(db1, db2));
    Mockito.doReturn(panacheQuery).when(repository)
        .find("location = ?1 and archivedAt is null", "AMSTERDAM-001");

    // When
    List<Warehouse> result = repository.findActiveByLocation("AMSTERDAM-001");

    // Then
    assertEquals(2, result.size());
    assertEquals("MWH.001", result.get(0).businessUnitCode);
    assertEquals("MWH.002", result.get(1).businessUnitCode);
  }

  @Test
  void shouldReturnEmptyListWhenNoWarehousesAtLocation() {
    // Given
    when(panacheQuery.list()).thenReturn(Collections.emptyList());
    Mockito.doReturn(panacheQuery).when(repository)
        .find("location = ?1 and archivedAt is null", "EMPTY-LOC");

    // When
    List<Warehouse> result = repository.findActiveByLocation("EMPTY-LOC");

    // Then
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  // --- helpers ---

  private Warehouse buildWarehouse(String buCode, String location, int capacity, int stock) {
    Warehouse w = new Warehouse();
    w.businessUnitCode = buCode;
    w.location = location;
    w.capacity = capacity;
    w.stock = stock;
    return w;
  }

  private DbWarehouse buildDbWarehouse(String buCode, String location, int capacity, int stock) {
    DbWarehouse db = new DbWarehouse();
    db.businessUnitCode = buCode;
    db.location = location;
    db.capacity = capacity;
    db.stock = stock;
    db.createdAt = LocalDateTime.of(2024, 7, 1, 0, 0);
    return db;
  }
}
