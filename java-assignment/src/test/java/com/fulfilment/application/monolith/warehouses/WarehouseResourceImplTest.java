package com.fulfilment.application.monolith.warehouses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.archive.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.create.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.replace.ReplaceWarehouseOperation;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class WarehouseResourceImplTest {

  private WarehouseRepository warehouseRepository;
  private CreateWarehouseOperation createWarehouseOperation;
  private ArchiveWarehouseOperation archiveWarehouseOperation;
  private ReplaceWarehouseOperation replaceWarehouseOperation;
  private WarehouseResourceImpl resource;

  @BeforeEach
  void setUp() {
    warehouseRepository = Mockito.mock(WarehouseRepository.class);
    createWarehouseOperation = Mockito.mock(CreateWarehouseOperation.class);
    archiveWarehouseOperation = Mockito.mock(ArchiveWarehouseOperation.class);
    replaceWarehouseOperation = Mockito.mock(ReplaceWarehouseOperation.class);
    resource = new WarehouseResourceImpl(
        warehouseRepository, createWarehouseOperation,
        archiveWarehouseOperation, replaceWarehouseOperation);
  }

  // --- listAllWarehousesUnits ---

  @Test
  void shouldListAllActiveWarehouses() {
    // Given
    Warehouse w1 = buildWarehouse("MWH.001", "AMSTERDAM-001", 50, 20);
    Warehouse w2 = buildWarehouse("MWH.002", "ZWOLLE-001", 30, 10);
    when(warehouseRepository.getAll()).thenReturn(List.of(w1, w2));

    // When
    List<com.warehouse.api.beans.Warehouse> result = resource.listAllWarehousesUnits();

    // Then
    assertEquals(2, result.size());
    assertEquals("MWH.001", result.get(0).getBusinessUnitCode());
    assertEquals("AMSTERDAM-001", result.get(0).getLocation());
    assertEquals(50, result.get(0).getCapacity());
    assertEquals(20, result.get(0).getStock());
    assertEquals("MWH.002", result.get(1).getBusinessUnitCode());
  }

  @Test
  void shouldReturnEmptyListWhenNoWarehouses() {
    // Given
    when(warehouseRepository.getAll()).thenReturn(Collections.emptyList());

    // When
    List<com.warehouse.api.beans.Warehouse> result = resource.listAllWarehousesUnits();

    // Then
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  // --- createANewWarehouseUnit ---

  @Test
  void shouldCreateWarehouseSuccessfully() {
    // Given
    com.warehouse.api.beans.Warehouse request = buildApiWarehouse("NEW.001", "AMSTERDAM-001", 40, 10);

    // When
    com.warehouse.api.beans.Warehouse result = resource.createANewWarehouseUnit(request);

    // Then
    ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
    verify(createWarehouseOperation).create(captor.capture());
    Warehouse captured = captor.getValue();
    assertEquals("NEW.001", captured.businessUnitCode);
    assertEquals("AMSTERDAM-001", captured.location);
    assertEquals(40, captured.capacity);
    assertEquals(10, captured.stock);

    assertNotNull(result);
    assertEquals("NEW.001", result.getBusinessUnitCode());
  }

  @Test
  void shouldReturn400WhenCreateValidationFails() {
    // Given
    com.warehouse.api.beans.Warehouse request = buildApiWarehouse("DUP.001", "AMSTERDAM-001", 40, 10);
    doThrow(new WarehouseValidationException("Duplicate business unit code"))
        .when(createWarehouseOperation).create(any());

    // When / Then
    WebApplicationException exception = assertThrows(
        WebApplicationException.class, () -> resource.createANewWarehouseUnit(request));
    assertEquals(400, exception.getResponse().getStatus());
  }

  // --- getAWarehouseUnitByID ---

  @Test
  void shouldGetWarehouseById() {
    // Given
    DbWarehouse entity = buildDbWarehouse(5L, "MWH.001", "AMSTERDAM-001", 50, 20);
    when(warehouseRepository.findById(5L)).thenReturn(entity);

    // When
    com.warehouse.api.beans.Warehouse result = resource.getAWarehouseUnitByID("5");

    // Then
    assertNotNull(result);
    assertEquals("5", result.getId());
    assertEquals("MWH.001", result.getBusinessUnitCode());
    assertEquals("AMSTERDAM-001", result.getLocation());
    assertEquals(50, result.getCapacity());
    assertEquals(20, result.getStock());
  }

  @Test
  void shouldReturn404WhenWarehouseNotFound() {
    // Given
    when(warehouseRepository.findById(99L)).thenReturn(null);

    // When / Then
    WebApplicationException exception = assertThrows(
        WebApplicationException.class, () -> resource.getAWarehouseUnitByID("99"));
    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  void shouldReturn404WhenWarehouseIsArchived() {
    // Given
    DbWarehouse entity = buildDbWarehouse(5L, "MWH.001", "AMSTERDAM-001", 50, 20);
    entity.archivedAt = LocalDateTime.now();
    when(warehouseRepository.findById(5L)).thenReturn(entity);

    // When / Then
    WebApplicationException exception = assertThrows(
        WebApplicationException.class, () -> resource.getAWarehouseUnitByID("5"));
    assertEquals(404, exception.getResponse().getStatus());
  }

  // --- archiveAWarehouseUnitByID ---

  @Test
  void shouldArchiveWarehouseById() {
    // Given
    DbWarehouse entity = buildDbWarehouse(3L, "MWH.001", "ZWOLLE-001", 30, 10);
    when(warehouseRepository.findById(3L)).thenReturn(entity);

    // When
    resource.archiveAWarehouseUnitByID("3");

    // Then
    verify(archiveWarehouseOperation).archive(any(Warehouse.class));
  }

  @Test
  void shouldReturn404WhenArchivingNonExistentWarehouse() {
    // Given
    when(warehouseRepository.findById(99L)).thenReturn(null);

    // When / Then
    WebApplicationException exception = assertThrows(
        WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("99"));
    assertEquals(404, exception.getResponse().getStatus());
    verify(archiveWarehouseOperation, never()).archive(any());
  }

  @Test
  void shouldReturn404WhenArchivingAlreadyArchivedWarehouse() {
    // Given
    DbWarehouse entity = buildDbWarehouse(3L, "MWH.001", "ZWOLLE-001", 30, 10);
    entity.archivedAt = LocalDateTime.now();
    when(warehouseRepository.findById(3L)).thenReturn(entity);

    // When / Then
    WebApplicationException exception = assertThrows(
        WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("3"));
    assertEquals(404, exception.getResponse().getStatus());
    verify(archiveWarehouseOperation, never()).archive(any());
  }

  @Test
  void shouldReturn400WhenArchiveOperationThrowsValidation() {
    // Given
    DbWarehouse entity = buildDbWarehouse(3L, "MWH.001", "ZWOLLE-001", 30, 10);
    when(warehouseRepository.findById(3L)).thenReturn(entity);
    doThrow(new WarehouseValidationException("Already archived"))
        .when(archiveWarehouseOperation).archive(any());

    // When / Then
    WebApplicationException exception = assertThrows(
        WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("3"));
    assertEquals(400, exception.getResponse().getStatus());
  }

  // --- replaceTheCurrentActiveWarehouse ---

  @Test
  void shouldReplaceWarehouseSuccessfully() {
    // Given
    com.warehouse.api.beans.Warehouse request = buildApiWarehouse(null, "ZWOLLE-001", 40, 10);

    // When
    com.warehouse.api.beans.Warehouse result =
        resource.replaceTheCurrentActiveWarehouse("MWH.001", request);

    // Then
    ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
    verify(replaceWarehouseOperation).replace(captor.capture());
    Warehouse captured = captor.getValue();
    assertEquals("MWH.001", captured.businessUnitCode);
    assertEquals("ZWOLLE-001", captured.location);
    assertEquals(40, captured.capacity);

    assertNotNull(result);
    assertEquals("MWH.001", result.getBusinessUnitCode());
  }

  @Test
  void shouldReturn404WhenReplacingNonExistentWarehouse() {
    // Given
    com.warehouse.api.beans.Warehouse request = buildApiWarehouse(null, "ZWOLLE-001", 40, 10);
    doThrow(new WarehouseValidationException("Warehouse not found"))
        .when(replaceWarehouseOperation).replace(any());

    // When / Then
    WebApplicationException exception = assertThrows(
        WebApplicationException.class,
        () -> resource.replaceTheCurrentActiveWarehouse("NON.EXISTENT", request));
    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  void shouldReturn400WhenReplaceValidationFails() {
    // Given
    com.warehouse.api.beans.Warehouse request = buildApiWarehouse(null, "ZWOLLE-001", 5, 10);
    doThrow(new WarehouseValidationException("Capacity too small"))
        .when(replaceWarehouseOperation).replace(any());

    // When / Then
    WebApplicationException exception = assertThrows(
        WebApplicationException.class,
        () -> resource.replaceTheCurrentActiveWarehouse("MWH.001", request));
    assertEquals(400, exception.getResponse().getStatus());
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

  private com.warehouse.api.beans.Warehouse buildApiWarehouse(
      String buCode, String location, int capacity, int stock) {
    com.warehouse.api.beans.Warehouse w = new com.warehouse.api.beans.Warehouse();
    w.setBusinessUnitCode(buCode);
    w.setLocation(location);
    w.setCapacity(capacity);
    w.setStock(stock);
    return w;
  }

  private DbWarehouse buildDbWarehouse(Long id, String buCode, String location, int capacity, int stock) {
    DbWarehouse db = new DbWarehouse();
    db.id = id;
    db.businessUnitCode = buCode;
    db.location = location;
    db.capacity = capacity;
    db.stock = stock;
    db.createdAt = LocalDateTime.of(2024, 7, 1, 0, 0);
    return db;
  }
}
