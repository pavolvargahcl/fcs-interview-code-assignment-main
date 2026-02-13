package com.fulfilment.application.monolith.stores;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LegacyStoreChangeObserverTest {

  private LegacyStoreManagerGateway gateway;
  private LegacyStoreChangeObserver observer;

  @BeforeEach
  void setUp() {
    gateway = mock(LegacyStoreManagerGateway.class);
    observer = new LegacyStoreChangeObserver(gateway);
  }

  @Test
  void shouldDelegateCreateEventToGateway() {
    // Given
    Store store = new Store("New Store");
    store.quantityProductsInStock = 10;
    StoreChangeEvent event = new StoreChangeEvent(store, StoreChangeEvent.OperationType.CREATE);

    // When
    observer.onStoreChange(event);

    // Then
    verify(gateway).createStoreOnLegacySystem(store);
    verifyNoMoreInteractions(gateway);
  }

  @Test
  void shouldDelegateUpdateEventToGateway() {
    // Given
    Store store = new Store("Updated Store");
    store.quantityProductsInStock = 25;
    StoreChangeEvent event = new StoreChangeEvent(store, StoreChangeEvent.OperationType.UPDATE);

    // When
    observer.onStoreChange(event);

    // Then
    verify(gateway).updateStoreOnLegacySystem(store);
    verifyNoMoreInteractions(gateway);
  }
}
