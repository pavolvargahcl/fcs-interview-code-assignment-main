package com.fulfilment.application.monolith.warehouses;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return find("archivedAt is null").list().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    persist(DbWarehouse.fromWarehouse(warehouse));
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse entity = find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();
    if (entity != null) {
      entity.location = warehouse.location;
      entity.capacity = warehouse.capacity;
      entity.stock = warehouse.stock;
      entity.archivedAt = warehouse.archivedAt;
    }
  }

  @Override
  public void remove(Warehouse warehouse) {
    DbWarehouse entity = find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();
    if (entity != null) {
      delete(entity);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse entity = find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    return entity != null ? entity.toWarehouse() : null;
  }

  @Override
  public List<Warehouse> findActiveByLocation(String location) {
    return find("location = ?1 and archivedAt is null", location).list().stream().map(DbWarehouse::toWarehouse).toList();
  }
}
