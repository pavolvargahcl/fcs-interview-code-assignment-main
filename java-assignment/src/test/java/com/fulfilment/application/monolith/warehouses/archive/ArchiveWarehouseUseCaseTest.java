package com.fulfilment.application.monolith.warehouses.archive;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.Warehouse;
import com.fulfilment.application.monolith.warehouses.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.WarehouseValidationException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ArchiveWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = Mockito.mock(WarehouseStore.class);
    useCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  void shouldArchiveExistingActiveWarehouse() {
    // Given
    Warehouse input = new Warehouse();
    input.businessUnitCode = "MWH.001";

    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 40;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.of(2024, 7, 1, 0, 0);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    // When
    useCase.archive(input);

    // Then
    ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseStore).update(captor.capture());
    assertNotNull(captor.getValue().archivedAt);
  }

  @Test
  void shouldRejectArchiveWhenWarehouseNotFound() {
    // Given
    Warehouse input = new Warehouse();
    input.businessUnitCode = "NON.EXISTENT";

    when(warehouseStore.findByBusinessUnitCode("NON.EXISTENT")).thenReturn(null);

    // When / Then
    assertThrows(WarehouseValidationException.class, () -> useCase.archive(input));
    verify(warehouseStore, never()).update(any());
  }

  @Test
  void shouldRejectArchiveWhenAlreadyArchived() {
    // Given
    Warehouse input = new Warehouse();
    input.businessUnitCode = "MWH.001";

    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.archivedAt = LocalDateTime.now();

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    // When / Then
    assertThrows(WarehouseValidationException.class, () -> useCase.archive(input));
    verify(warehouseStore, never()).update(any());
  }
}
