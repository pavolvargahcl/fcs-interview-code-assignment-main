package com.fulfilment.application.monolith.warehouses.replace;

import com.fulfilment.application.monolith.warehouses.Location;
import com.fulfilment.application.monolith.warehouses.LocationResolver;
import com.fulfilment.application.monolith.warehouses.Warehouse;
import com.fulfilment.application.monolith.warehouses.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.WarehouseValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      throw new WarehouseValidationException(
          "Warehouse with business unit code '" + newWarehouse.businessUnitCode + "' not found.");
    }

    validateCapacityAccommodation(newWarehouse, existing);
    validateStockMatching(newWarehouse, existing);

    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new WarehouseValidationException(
          "Location '" + newWarehouse.location + "' is not a valid location.");
    }

    List<Warehouse> activeAtLocation = warehouseStore.findActiveByLocation(newWarehouse.location);
    int currentCapacitySum = activeAtLocation.stream().mapToInt(w -> w.capacity).sum();
    int adjustedCapacitySum = currentCapacitySum - existing.capacity;
    if (adjustedCapacitySum + newWarehouse.capacity > location.maxCapacity) {
      throw new WarehouseValidationException(
          "New warehouse capacity would exceed the maximum capacity ("
              + location.maxCapacity + ") at location '" + location.identification + "'.");
    }

    if (newWarehouse.stock > newWarehouse.capacity) {
      throw new WarehouseValidationException(
          "Stock (" + newWarehouse.stock + ") cannot exceed capacity (" + newWarehouse.capacity + ").");
    }

    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);

    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }

  private void validateCapacityAccommodation(Warehouse newWarehouse, Warehouse existing) {
    if (newWarehouse.capacity < existing.stock) {
      throw new WarehouseValidationException(
          "New warehouse capacity (" + newWarehouse.capacity
              + ") cannot accommodate the existing stock (" + existing.stock + ").");
    }
  }

  private void validateStockMatching(Warehouse newWarehouse, Warehouse existing) {
    if (!newWarehouse.stock.equals(existing.stock)) {
      throw new WarehouseValidationException(
          "New warehouse stock (" + newWarehouse.stock
              + ") must match the existing warehouse stock (" + existing.stock + ").");
    }
  }
}
