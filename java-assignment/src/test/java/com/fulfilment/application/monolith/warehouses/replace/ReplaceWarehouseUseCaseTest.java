package com.fulfilment.application.monolith.warehouses.replace;

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
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ReplaceWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = Mockito.mock(WarehouseStore.class);
    locationResolver = Mockito.mock(LocationResolver.class);
    useCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void shouldReplaceWarehouseSuccessfully() {
    // Given
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    existing.createdAt = LocalDateTime.of(2024, 7, 1, 0, 0);

    Warehouse replacement = buildWarehouse("MWH.001", "ZWOLLE-001", 40, 10);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    when(warehouseStore.findActiveByLocation("ZWOLLE-001")).thenReturn(List.of(existing));

    // When
    useCase.replace(replacement);

    // Then
    verify(warehouseStore).update(existing);
    assertNotNull(existing.archivedAt);
    verify(warehouseStore).create(replacement);
    assertNotNull(replacement.createdAt);
  }

  @Test
  void shouldRejectReplaceWhenWarehouseNotFound() {
    // Given
    Warehouse replacement = buildWarehouse("NON.EXISTENT", "ZWOLLE-001", 40, 10);

    when(warehouseStore.findByBusinessUnitCode("NON.EXISTENT")).thenReturn(null);

    // When / Then
    assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectWhenNewCapacityCannotAccommodateExistingStock() {
    // Given
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 20);
    Warehouse replacement = buildWarehouse("MWH.001", "ZWOLLE-001", 15, 20);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    // When / Then
    assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectWhenStockDoesNotMatch() {
    // Given
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = buildWarehouse("MWH.001", "ZWOLLE-001", 40, 5);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    // When / Then
    assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldRejectWhenNewLocationIsInvalid() {
    // Given
    Warehouse existing = buildWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = buildWarehouse("MWH.001", "INVALID-LOC", 40, 10);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("INVALID-LOC")).thenReturn(null);

    // When / Then
    assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement));
    verify(warehouseStore, never()).update(any());
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
