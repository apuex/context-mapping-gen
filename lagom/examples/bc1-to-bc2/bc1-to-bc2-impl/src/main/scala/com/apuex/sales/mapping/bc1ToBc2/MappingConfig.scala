package com.apuex.sales.mapping.bc1ToBc2

import com.github.apuex.events.play.EventEnvelopeProto
import com.google.protobuf.util.JsonFormat


class MappingConfig {
  val registry = JsonFormat.TypeRegistry
    .newBuilder
    .add(MappingMessages.getDescriptor.getMessageTypes)
    .add(EventEnvelopeProto.getDescriptor.getMessageTypes)
    .build

  val printer = JsonFormat.printer().usingTypeRegistry(registry)

  val parser = JsonFormat.parser().usingTypeRegistry(registry)

}
