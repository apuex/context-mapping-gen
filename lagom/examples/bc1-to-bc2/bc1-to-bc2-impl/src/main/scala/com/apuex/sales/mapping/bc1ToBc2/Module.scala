package com.apuex.sales.mapping.bc1ToBc2

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    bindActor[OrderInventoryMapping](OrderInventoryMapping.name)
  }
}
