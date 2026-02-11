package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;

@ApplicationScoped
public class LegacyStoreChangeObserver {

  private final LegacyStoreManagerGateway legacyStoreManagerGateway;

  public LegacyStoreChangeObserver(LegacyStoreManagerGateway legacyStoreManagerGateway) {
    this.legacyStoreManagerGateway = legacyStoreManagerGateway;
  }

  public void onStoreChange(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) StoreChangeEvent event) {
    switch (event.operationType()) {
      case CREATE -> legacyStoreManagerGateway.createStoreOnLegacySystem(event.store());
      case UPDATE -> legacyStoreManagerGateway.updateStoreOnLegacySystem(event.store());
    }
  }
}
