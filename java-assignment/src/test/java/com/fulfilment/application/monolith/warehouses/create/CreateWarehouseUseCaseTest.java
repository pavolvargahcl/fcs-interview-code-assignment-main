package com.fulfilment.application.monolith.warehouses.create;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.Location;
import com.fulfilment.application.monolith.warehouses.LocationResolver;
import com.fulfilment.application.monolith.warehouses.Warehouse;
import com.fulfilment.application.monolith.warehouses.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.WarehouseValidationException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CreateWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = Mockito.mock(WarehouseStore.class);
    locationResolver = Mockito.mock(LocationResolver.class);
    useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void shouldCreateWarehouseWhenAllValidationsPass() {
    // Given
    Warehouse warehouse = buildWarehouse("NEW.001", "AMSTERDAM-001", 20, 5);
    when(warehouseStore.findByBusinessUnitCode("NEW.001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.findActiveByLocation("AMSTERDAM-001")).thenReturn(Collections.emptyList());

    // When
    useCase.create(warehouse);

    // Then
    verify(warehouseStore).create(warehouse);
    assertNotNull(warehouse.createdAt);
  }

  @Test
  void shouldRejectDuplicateBusinessUnitCode() {
    // Given
    Warehouse warehouse = buildWarehouse("MWH.001", "AMSTERDAM-001", 20, 5);
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 40, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    // When / Then
    assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectInvalidLocation() {
    // Given
    Warehouse warehouse = buildWarehouse("NEW.001", "INVALID-LOC", 20, 5);
    when(warehouseStore.findByBusinessUnitCode("NEW.001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("INVALID-LOC")).thenReturn(null);

    // When / Then
    assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectWhenMaxWarehousesReached() {
    // Given
    Warehouse warehouse = buildWarehouse("NEW.001", "ZWOLLE-001", 20, 5);
    when(warehouseStore.findByBusinessUnitCode("NEW.001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    Warehouse existingAtLocation = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    when(warehouseStore.findActiveByLocation("ZWOLLE-001")).thenReturn(List.of(existingAtLocation));

    // When / Then
    assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectWhenCapacityExceedsLocationMax() {
    // Given
    Warehouse warehouse = buildWarehouse("NEW.001", "AMSTERDAM-001", 80, 5);
    when(warehouseStore.findByBusinessUnitCode("NEW.001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    Warehouse existingAtLocation = buildWarehouse("MWH.012", "AMSTERDAM-001", 50, 5);
    when(warehouseStore.findActiveByLocation("AMSTERDAM-001")).thenReturn(List.of(existingAtLocation));

    // When / Then
    assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectWhenStockExceedsCapacity() {
    // Given
    Warehouse warehouse = buildWarehouse("NEW.001", "AMSTERDAM-001", 20, 25);
    when(warehouseStore.findByBusinessUnitCode("NEW.001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.findActiveByLocation("AMSTERDAM-001")).thenReturn(Collections.emptyList());

    // When / Then
    assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(any());
  }

  private Warehouse buildWarehouse(String buCode, String location, int capacity, int stock) {
    Warehouse w = new Warehouse();
    w.businessUnitCode = buCode;
    w.location = location;
    w.capacity = capacity;
    w.stock = stock;
    return w;
  }
}
