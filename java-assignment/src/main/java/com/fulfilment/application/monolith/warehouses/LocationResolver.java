package com.fulfilment.application.monolith.warehouses;

public interface LocationResolver {
  Location resolveByIdentifier(String identifier);
}
