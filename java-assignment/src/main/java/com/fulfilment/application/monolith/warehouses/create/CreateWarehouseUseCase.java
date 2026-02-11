package com.fulfilment.application.monolith.warehouses.create;

import com.fulfilment.application.monolith.warehouses.Location;
import com.fulfilment.application.monolith.warehouses.LocationResolver;
import com.fulfilment.application.monolith.warehouses.Warehouse;
import com.fulfilment.application.monolith.warehouses.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.WarehouseValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    validateBusinessUnitCodeUniqueness(warehouse.businessUnitCode);

    Location location = validateLocationExists(warehouse.location);

    List<Warehouse> activeAtLocation = warehouseStore.findActiveByLocation(warehouse.location);

    validateCreationFeasibility(activeAtLocation, location);
    validateCapacity(activeAtLocation, warehouse, location);
    validateStockWithinCapacity(warehouse);

    warehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(warehouse);
  }

  private void validateBusinessUnitCodeUniqueness(String businessUnitCode) {
    Warehouse existing = warehouseStore.findByBusinessUnitCode(businessUnitCode);
    if (existing != null) {
      throw new WarehouseValidationException(
          "A warehouse with business unit code '" + businessUnitCode + "' already exists.");
    }
  }

  Location validateLocationExists(String locationIdentifier) {
    Location location = locationResolver.resolveByIdentifier(locationIdentifier);
    if (location == null) {
      throw new WarehouseValidationException(
          "Location '" + locationIdentifier + "' is not a valid location.");
    }
    return location;
  }

  void validateCreationFeasibility(List<Warehouse> activeAtLocation, Location location) {
    if (activeAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new WarehouseValidationException(
          "Maximum number of warehouses (" + location.maxNumberOfWarehouses
              + ") already reached at location '" + location.identification + "'.");
    }
  }

  void validateCapacity(List<Warehouse> activeAtLocation, Warehouse warehouse, Location location) {
    int currentCapacitySum = activeAtLocation.stream().mapToInt(w -> w.capacity).sum();
    if (currentCapacitySum + warehouse.capacity > location.maxCapacity) {
      throw new WarehouseValidationException(
          "Adding capacity " + warehouse.capacity + " would exceed the maximum capacity ("
              + location.maxCapacity + ") at location '" + location.identification + "'.");
    }
  }

  void validateStockWithinCapacity(Warehouse warehouse) {
    if (warehouse.stock > warehouse.capacity) {
      throw new WarehouseValidationException(
          "Stock (" + warehouse.stock + ") cannot exceed capacity (" + warehouse.capacity + ").");
    }
  }
}
