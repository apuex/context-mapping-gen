package com.apuex.sales.mapping.bc1ToBc2

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class MappingModule extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[MappingConfig])
    bindActor[OrderInventoryMapping](OrderInventoryMapping.name)
  }
}
