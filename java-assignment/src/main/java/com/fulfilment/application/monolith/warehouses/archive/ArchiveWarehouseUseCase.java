package com.fulfilment.application.monolith.warehouses.archive;

import com.fulfilment.application.monolith.warehouses.Warehouse;
import com.fulfilment.application.monolith.warehouses.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.WarehouseValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing == null) {
      throw new WarehouseValidationException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' not found.");
    }
    if (existing.archivedAt != null) {
      throw new WarehouseValidationException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' is already archived.");
    }

    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);
  }
}
