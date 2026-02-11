package com.fulfilment.application.monolith.warehouses;

import java.util.List;

public interface WarehouseStore {

  List<Warehouse> getAll();

  void create(Warehouse warehouse);

  void update(Warehouse warehouse);

  void remove(Warehouse warehouse);

  Warehouse findByBusinessUnitCode(String buCode);

  List<Warehouse> findActiveByLocation(String location);
}
