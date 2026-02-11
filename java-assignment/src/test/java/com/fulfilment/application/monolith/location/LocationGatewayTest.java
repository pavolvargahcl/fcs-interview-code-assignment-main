package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fulfilment.application.monolith.warehouses.Location;
import org.junit.jupiter.api.Test;

class LocationGatewayTest {

  private final LocationGateway locationGateway = new LocationGateway();


  @Test
  void testWhenResolveExistingLocationShouldReturn() {
    // Given
    String identifier = "ZWOLLE-001";

    // When
    Location location = locationGateway.resolveByIdentifier(identifier);

    // Then
    assertNotNull(location);
    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  void testWhenResolveAnotherExistingLocationShouldReturn() {
    // Given
    String identifier = "AMSTERDAM-001";

    // When
    Location location = locationGateway.resolveByIdentifier(identifier);

    // Then
    assertNotNull(location);
    assertEquals("AMSTERDAM-001", location.identification);
    assertEquals(5, location.maxNumberOfWarehouses);
    assertEquals(100, location.maxCapacity);
  }

  @Test
  void testWhenResolveNonExistingLocationShouldReturnNull() {
    // Given
    String identifier = "NON-EXISTENT";

    // When
    Location location = locationGateway.resolveByIdentifier(identifier);

    // Then
    assertNull(location);
  }

  @Test
  void testWhenResolveWithNullIdentifierShouldReturnNull() {
    // Given
    String identifier = null;

    // When
    Location location = locationGateway.resolveByIdentifier(identifier);

    // Then
    assertNull(location);
  }
}
