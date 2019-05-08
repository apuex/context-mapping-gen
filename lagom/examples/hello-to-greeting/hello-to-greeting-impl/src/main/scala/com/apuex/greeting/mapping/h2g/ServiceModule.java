package com.apuex.greeting.mapping.h2g;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.api.ServiceLocator;
import com.lightbend.lagom.javadsl.client.ConfigurationServiceLocator;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class ServiceModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  public void configure() {
    bind(ServiceLocator.class).to(ConfigurationServiceLocator.class);
    bindClient(HelloService.class);
    bindService(H2GMappingService.class, H2GMappingServiceImpl.class);
  }
}
