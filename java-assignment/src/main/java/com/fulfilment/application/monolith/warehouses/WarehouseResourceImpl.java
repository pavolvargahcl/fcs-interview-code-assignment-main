package com.fulfilment.application.monolith.warehouses;

import com.fulfilment.application.monolith.warehouses.archive.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.create.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.replace.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private final WarehouseRepository warehouseRepository;
  private final CreateWarehouseOperation createWarehouseOperation;
  private final ArchiveWarehouseOperation archiveWarehouseOperation;
  private final ReplaceWarehouseOperation replaceWarehouseOperation;

  public WarehouseResourceImpl(
      WarehouseRepository warehouseRepository,
      CreateWarehouseOperation createWarehouseOperation,
      ArchiveWarehouseOperation archiveWarehouseOperation,
      ReplaceWarehouseOperation replaceWarehouseOperation) {
    this.warehouseRepository = warehouseRepository;
    this.createWarehouseOperation = createWarehouseOperation;
    this.archiveWarehouseOperation = archiveWarehouseOperation;
    this.replaceWarehouseOperation = replaceWarehouseOperation;
  }

  @Override
  public List<com.warehouse.api.beans.Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @Transactional
  public com.warehouse.api.beans.Warehouse createANewWarehouseUnit(@NotNull com.warehouse.api.beans.Warehouse data) {
    Warehouse domainWarehouse = toDomainWarehouse(data);
    try {
      createWarehouseOperation.create(domainWarehouse);
    } catch (WarehouseValidationException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  public com.warehouse.api.beans.Warehouse getAWarehouseUnitByID(String id) {
    DbWarehouse entity = warehouseRepository.findById(Long.valueOf(id));
    if (entity == null || entity.archivedAt != null) {
      throw new WebApplicationException("Warehouse with id " + id + " not found.", 404);
    }
    return toWarehouseResponseFromDb(entity);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    DbWarehouse entity = warehouseRepository.findById(Long.valueOf(id));
    if (entity == null || entity.archivedAt != null) {
      throw new WebApplicationException("Warehouse with id " + id + " not found.", 404);
    }
    Warehouse domainWarehouse = entity.toWarehouse();
    try {
      archiveWarehouseOperation.archive(domainWarehouse);
    } catch (WarehouseValidationException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  @Transactional
  public com.warehouse.api.beans.Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, @NotNull com.warehouse.api.beans.Warehouse data) {
    Warehouse domainWarehouse = toDomainWarehouse(data);
    domainWarehouse.businessUnitCode = businessUnitCode;
    try {
      replaceWarehouseOperation.replace(domainWarehouse);
    } catch (WarehouseValidationException e) {
      if (e.getMessage().contains("not found")) {
        throw new WebApplicationException(e.getMessage(), 404);
      }
      throw new WebApplicationException(e.getMessage(), 400);
    }
    return toWarehouseResponse(domainWarehouse);
  }

  private com.warehouse.api.beans.Warehouse toWarehouseResponse(Warehouse warehouse) {
    com.warehouse.api.beans.Warehouse response = new com.warehouse.api.beans.Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);
    return response;
  }

  private com.warehouse.api.beans.Warehouse toWarehouseResponseFromDb(DbWarehouse entity) {
    com.warehouse.api.beans.Warehouse response = new com.warehouse.api.beans.Warehouse();
    response.setId(String.valueOf(entity.id));
    response.setBusinessUnitCode(entity.businessUnitCode);
    response.setLocation(entity.location);
    response.setCapacity(entity.capacity);
    response.setStock(entity.stock);
    return response;
  }

  private Warehouse toDomainWarehouse(com.warehouse.api.beans.Warehouse data) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = data.getBusinessUnitCode();
    warehouse.location = data.getLocation();
    warehouse.capacity = data.getCapacity();
    warehouse.stock = data.getStock();
    return warehouse;
  }
}
