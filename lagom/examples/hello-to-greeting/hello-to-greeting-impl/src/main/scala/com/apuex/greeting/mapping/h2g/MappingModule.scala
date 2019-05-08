package com.apuex.greeting.mapping.h2g

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class MappingModule extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[MappingConfig])
    bindActor[H2GMapping](H2GMapping.name)
  }
}
