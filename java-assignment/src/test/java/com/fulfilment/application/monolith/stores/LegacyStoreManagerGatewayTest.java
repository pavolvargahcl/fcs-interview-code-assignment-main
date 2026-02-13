package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LegacyStoreManagerGatewayTest {

  private LegacyStoreManagerGateway gateway;

  @BeforeEach
  void setUp() {
    gateway = new LegacyStoreManagerGateway();
  }

  @Test
  void shouldCreateStoreOnLegacySystemWithoutError() {
    // Given
    Store store = new Store("Legacy Create Store");
    store.quantityProductsInStock = 15;

    // When / Then
    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }

  @Test
  void shouldUpdateStoreOnLegacySystemWithoutError() {
    // Given
    Store store = new Store("Legacy Update Store");
    store.quantityProductsInStock = 30;

    // When / Then
    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }
}
