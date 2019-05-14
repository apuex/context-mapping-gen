package com.apuex.sales.mapping.bc1ToBc2;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.api.ServiceLocator;
import com.lightbend.lagom.javadsl.client.ConfigurationServiceLocator;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class ServiceModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  public void configure() {
    bind(ServiceLocator.class).to(ConfigurationServiceLocator.class);
    bindClient(InventoryService.class);
    bindClient(OrderService.class);
    bindClient(ProductService.class);
    bindService(Bc1ToBc2MappingService.class, Bc1ToBc2MappingServiceImpl.class);
  }
}
