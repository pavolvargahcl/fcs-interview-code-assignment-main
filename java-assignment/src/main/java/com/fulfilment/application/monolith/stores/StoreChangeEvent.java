package com.fulfilment.application.monolith.stores;

public record StoreChangeEvent(Store store, OperationType operationType) {

  public enum OperationType {
    CREATE,
    UPDATE
  }
}
